package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.DisputeRequest;
import com.example.cyclemartberemake.dto.response.DisputeResponse;
import com.example.cyclemartberemake.entity.*;
import com.example.cyclemartberemake.repository.DisputeRepository;
import com.example.cyclemartberemake.repository.PaymentRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.DisputeService;
import com.example.cyclemartberemake.service.PaymentService;
import com.example.cyclemartberemake.service.RealtimeNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DisputeServiceImpl implements DisputeService {

    private final DisputeRepository disputeRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;
    private final RealtimeNotificationService realtimeNotificationService;

    @Override
    @Transactional
    public DisputeResponse openDispute(Long buyerId, DisputeRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (!payment.getUser().getId().equals(buyerId)) {
            throw new RuntimeException("Bạn không có quyền tạo tranh chấp cho giao dịch này");
        }

        if (payment.getOrderStatus() != OrderStatus.DELIVERED) {
            throw new RuntimeException("Chỉ có thể tạo tranh chấp sau khi đã xác nhận nhận hàng");
        }

        // Only allow dispute for verified-at-purchase listings
        if (!Boolean.TRUE.equals(payment.getVerifiedAtPurchase())) {
            throw new RuntimeException("Chỉ có thể tạo tranh chấp cho xe đã được kiểm định tại thời điểm mua");
        }

        // Must have escrow points still held
        if (payment.getEscrowPoints() == null || payment.getEscrowPoints() <= 0) {
            throw new RuntimeException("Không còn điểm escrow để tranh chấp");
        }

        // Allow dispute within 7 days of deliveredAt
        if (payment.getDeliveredAt() == null ||
                payment.getDeliveredAt().isBefore(LocalDateTime.now().minusDays(7))) {
            throw new RuntimeException("Đã quá thời hạn 7 ngày kể từ khi nhận hàng để tạo tranh chấp");
        }

        if (disputeRepository.existsByPaymentIdAndBuyerId(payment.getId(), buyerId)) {
            throw new RuntimeException("Bạn đã tạo tranh chấp cho giao dịch này rồi");
        }

        // Lấy seller từ bikePost
        if (payment.getBikePost() == null || payment.getBikePost().getUser() == null) {
            throw new RuntimeException("Không xác định được người bán");
        }
        Users seller = payment.getBikePost().getUser();
        Users buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        Dispute dispute = Dispute.builder()
                .payment(payment)
                .buyer(buyer)
                .seller(seller)
                .reason(request.getReason())
                .evidenceUrls(request.getEvidenceUrls())
                .status(DisputeStatus.OPENED)
                .verifiedAtPurchase(payment.getVerifiedAtPurchase())
                .build();

        // Cập nhật order status sang DISPUTE_SYSTEM
        payment.setOrderStatus(OrderStatus.DISPUTE_SYSTEM);
        paymentRepository.save(payment);

        Dispute saved = disputeRepository.save(dispute);

        realtimeNotificationService.notifyDisputeStatusChange(buyerId, seller.getId(), saved.getId(), DisputeStatus.OPENED.name());
        realtimeNotificationService.notifyOrderStatusChange(buyerId, seller.getId(), payment.getId(), OrderStatus.DISPUTE_SYSTEM.name());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public DisputeResponse sellerApprove(Long disputeId, Long sellerId) {
        Dispute dispute = findAndValidateSeller(disputeId, sellerId);
        validateOpenDispute(dispute);

        dispute.setStatus(DisputeStatus.SELLER_APPROVED);
        disputeRepository.save(dispute);

        Payment payment = dispute.getPayment();
        payment.setOrderStatus(OrderStatus.RETURN_REQUESTED);
        paymentRepository.save(payment);

        realtimeNotificationService.notifyDisputeStatusChange(dispute.getBuyer().getId(), sellerId, disputeId, DisputeStatus.SELLER_APPROVED.name());
        realtimeNotificationService.notifyOrderStatusChange(dispute.getBuyer().getId(), sellerId, payment.getId(), OrderStatus.RETURN_REQUESTED.name());

        return toResponse(dispute);
    }

    @Override
    @Transactional
    public DisputeResponse sellerReject(Long disputeId, Long sellerId) {
        Dispute dispute = findAndValidateSeller(disputeId, sellerId);
        validateOpenDispute(dispute);

        dispute.setStatus(DisputeStatus.SELLER_REJECTED);
        disputeRepository.save(dispute);

        dispute.setStatus(DisputeStatus.ADMIN_REVIEW);
        disputeRepository.save(dispute);

        realtimeNotificationService.notifyDisputeStatusChange(dispute.getBuyer().getId(), sellerId, disputeId, DisputeStatus.ADMIN_REVIEW.name());

        return toResponse(dispute);
    }

    @Override
    @Transactional
    public DisputeResponse adminResolve(Long disputeId, Long adminId, String resolution, String resolutionNote) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tranh chấp"));

        DisputeStatus resolvedStatus = switch (resolution.toUpperCase()) {
            case "REFUND_BUYER" -> DisputeStatus.RESOLVED_REFUND_BUYER;
            case "RELEASE_SELLER" -> DisputeStatus.RESOLVED_RELEASE_SELLER;
            case "PARTIAL" -> DisputeStatus.RESOLVED_PARTIAL;
            default -> throw new RuntimeException("Resolution không hợp lệ: REFUND_BUYER | RELEASE_SELLER | PARTIAL");
        };

        Users admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin không tồn tại"));

        dispute.setStatus(resolvedStatus);
        dispute.setResolutionNote(resolutionNote);
        dispute.setResolvedBy(admin);
        dispute.setResolvedAt(LocalDateTime.now());
        disputeRepository.save(dispute);

        Payment payment = dispute.getPayment();
        if (resolvedStatus == DisputeStatus.RESOLVED_REFUND_BUYER ||
                resolvedStatus == DisputeStatus.RESOLVED_PARTIAL) {
            paymentService.refundEscrow(payment.getId(), adminId);
        } else {
            paymentService.releaseEscrow(payment.getId(), adminId);
        }

        realtimeNotificationService.notifyDisputeStatusChange(dispute.getBuyer().getId(), dispute.getSeller().getId(), disputeId, resolvedStatus.name());

        return toResponse(dispute);
    }

    @Override
    public Page<DisputeResponse> getMyDisputesAsBuyer(Long buyerId, Pageable pageable) {
        return disputeRepository.findByBuyerId(buyerId, pageable).map(this::toResponse);
    }

    @Override
    public Page<DisputeResponse> getMyDisputesAsSeller(Long sellerId, Pageable pageable) {
        return disputeRepository.findBySellerId(sellerId, pageable).map(this::toResponse);
    }

    @Override
    public Page<DisputeResponse> getAllForAdmin(Pageable pageable) {
        return disputeRepository.findAll(pageable).map(this::toResponse);
    }

    private Dispute findAndValidateSeller(Long disputeId, Long sellerId) {
        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tranh chấp"));
        if (!dispute.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("Bạn không có quyền thực hiện hành động này");
        }
        return dispute;
    }

    private void validateOpenDispute(Dispute dispute) {
        if (dispute.getStatus() != DisputeStatus.OPENED) {
            throw new RuntimeException("Tranh chấp này không còn ở trạng thái OPENED");
        }
    }

    private DisputeResponse toResponse(Dispute d) {
        return DisputeResponse.builder()
                .id(d.getId())
                .paymentId(d.getPayment().getId())
                .paymentOrderId(d.getPayment().getOrderId())
                .buyerId(d.getBuyer().getId())
                .buyerName(d.getBuyer().getFullName())
                .sellerId(d.getSeller().getId())
                .sellerName(d.getSeller().getFullName())
                .reason(d.getReason())
                .evidenceUrls(d.getEvidenceUrls())
                .status(d.getStatus().name())
                .verifiedAtPurchase(d.getVerifiedAtPurchase())
                .buyerFault(d.getBuyerFault())
                .resolutionNote(d.getResolutionNote())
                .resolvedByName(d.getResolvedBy() != null ? d.getResolvedBy().getFullName() : null)
                .resolvedAt(d.getResolvedAt())
                .createdAt(d.getCreatedAt())
                .build();
    }
}
