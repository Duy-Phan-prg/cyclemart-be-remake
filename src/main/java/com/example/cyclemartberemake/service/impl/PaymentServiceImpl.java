package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.CreatePaymentRequest;
import com.example.cyclemartberemake.dto.request.DeliveryUpdateRequest;
import com.example.cyclemartberemake.dto.response.CreatePaymentResponse;
import com.example.cyclemartberemake.dto.response.PaymentResponse;
import com.example.cyclemartberemake.entity.*;
import com.example.cyclemartberemake.mapper.PaymentMapper;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.PaymentRepository;
import com.example.cyclemartberemake.repository.PointTransactionRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.repository.PostPrioritySubscriptionRepository;
import com.example.cyclemartberemake.repository.InspectionRepository;
import com.example.cyclemartberemake.repository.SellerRatingRepository;
import com.example.cyclemartberemake.service.PaymentNotificationService;
import com.example.cyclemartberemake.service.PaymentService;
import com.example.cyclemartberemake.service.UserService;
import com.example.cyclemartberemake.service.RealtimeNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final UserRepository userRepository;
    private final BikePostRepository bikePostRepository;
    private final UserService userService;
    private final PaymentMapper paymentMapper;
    private final PaymentNotificationService notificationService;
    private final PostPrioritySubscriptionRepository subscriptionRepository;
    private final InspectionRepository inspectionRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final RealtimeNotificationService realtimeNotificationService;
    private final SellerRatingRepository sellerRatingRepository;

    @Value("${vnpay.tmnCode}")
    private String vnpayTmnCode;

    @Value("${vnpay.hashSecret}")
    private String vnpayHashSecret;

    @Value("${vnpay.apiUrl}")
    private String vnpayApiUrl;

    @Value("${vnpay.returnUrl}")
    private String vnpayReturnUrl;

    @Value("${vnpay.ipnUrl}")
    private String vnpayIpnUrl;

    @Override
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) throws Exception {

        Long userId = getCurrentUserId();

        PaymentType paymentType = PaymentType.OTHER;
        if (request.getType() != null) {
            try {
                paymentType = PaymentType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Loại thanh toán không hợp lệ: {}", request.getType());
            }
        }

        BikePost bikePost = null;

        // DIRECT_PAYMENT (COD) đi qua createDirectPayment riêng, không dùng VNPay
        if (paymentType == PaymentType.DIRECT_PAYMENT) {
            throw new RuntimeException("Đơn hàng COD vui lòng dùng endpoint /api/v1/payments/direct");
        }

        // Chỉ gán bikePost vào Payment nếu KHÔNG PHẢI là Mua Gói và KHÔNG PHẢI là Kiểm định.
        if (request.getBikePostId() != null
                && paymentType != PaymentType.PRIORITY_PACKAGE
                && paymentType != PaymentType.INSPECTION_FEE) {
            bikePost = bikePostRepository.findById(request.getBikePostId())
                    .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

            // Xe chưa kiểm định chỉ được mua COD, không qua VNPay
            if (paymentType == PaymentType.ORDER_PAYMENT && !Boolean.TRUE.equals(bikePost.getIsVerified())) {
                throw new RuntimeException("Xe chưa được kiểm định chỉ hỗ trợ thanh toán tiền mặt (COD). Vui lòng dùng tính năng đặt hàng COD.");
            }
        }

        Long amount = request.getAmount();
        if (amount == null && bikePost != null) {
            amount = bikePost.getPrice().longValue();
        }

        if (amount == null || amount < 10000 || amount > 50000000) {
            throw new RuntimeException("Số tiền không hợp lệ. Phải từ 10,000 - 50,000,000 VND");
        }

        String orderId = "ORDER_" + System.currentTimeMillis();
        String description = request.getDescription() != null ?
                request.getDescription().replaceAll("[^a-zA-Z0-9\\s_ÀÁÂÃÈÉÊÌÍÒÓÔÕÙÚĂĐĨŨƠàáâãèéêìíòóôõùúăđĩũơƯĂẠẢẤẦẨẪẬẮẰẲẴẶẸẺẼỀỀỂẾưăạảấầẩẫậắằẳẵặẹẻẽềềểếỄỆỈỊỌỎỐỒỔỖỘỚỜỞỠỢỤỦỨỪễệỉịọỏốồổỗộớờởỡợụủứừỬỮỰỲỴÝỶỸửữựỳỵỷỹ]", " ") : "Thanh toan CycleMart";

        // Lấy thông tin seller từ bikePost (nếu có)
        Users seller = null;
        if (bikePost != null && bikePost.getUserId() != null) {
            seller = userRepository.findById(bikePost.getUserId()).orElse(null);
        }

        Payment payment = Payment.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")))
                .seller(seller) // Set seller
                .bikePost(bikePost)
                .orderId(orderId)
                .amount(amount)
                .description(description)
                .status(PaymentStatus.PENDING)
                .type(paymentType)
                .orderStatus(paymentType == PaymentType.ORDER_PAYMENT ? OrderStatus.PENDING_PAYMENT : null)
                .referenceId(request.getReferenceId())
                .address(request.getAddress())
                .build();

        paymentRepo.save(payment);

        try {
            String vnpayUrl = generateVNPayUrl(orderId, amount);

            return CreatePaymentResponse.builder()
                    .orderId(orderId)
                    .amount(amount)
                    .description(description)
                    .paymentUrl(vnpayUrl)
                    .message("Tạo link thanh toán thành công")
                    .success(true)
                    .build();

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setMessage("Lỗi hệ thống: " + e.getMessage());
            paymentRepo.save(payment);
            throw new RuntimeException("Lỗi tạo thanh toán: " + e.getMessage());
        }
    }

    @Override
    public Page<PaymentResponse> getPaymentHistory(Pageable pageable) {
        Long userId = getCurrentUserId();
        // Lấy tất cả giao dịch của user
        Page<Payment> payments = paymentRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        // Lọc: CHỈ giữ lại các giao dịch mua xe (ORDER_PAYMENT + DIRECT_PAYMENT)
        List<PaymentResponse> filteredList = payments.getContent().stream()
                .filter(p -> p.getType() == PaymentType.ORDER_PAYMENT || p.getType() == PaymentType.DIRECT_PAYMENT)
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());

        enrichWithHasRated(filteredList);
        return new org.springframework.data.domain.PageImpl<>(filteredList, pageable, payments.getTotalElements());
    }

    @Override
    public Page<PaymentResponse> getPaymentHistoryAsSeller(Pageable pageable) {
        Long sellerId = getCurrentUserId();
        Page<Payment> payments = paymentRepo.findBySellerIdOrderByCreatedAtDesc(sellerId, pageable);
        List<PaymentResponse> list = payments.getContent().stream()
                .map(paymentMapper::toResponse)
                .collect(Collectors.toList());
        enrichWithHasRated(list);
        return new org.springframework.data.domain.PageImpl<>(list, pageable, payments.getTotalElements());
    }

    @Override
    public Page<PaymentResponse> getPaymentHistoryByStatus(String status, Pageable pageable) {
        Long userId = getCurrentUserId();
        PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        Page<Payment> payments = paymentRepo.findByUserIdAndStatusOrderByCreatedAtDesc(userId, paymentStatus, pageable);

        // chỉ hiện giao dịch mua xe trong lịch sử đơn hàng
        List<PaymentResponse> filteredList = payments.getContent().stream()
                .filter(p -> p.getType() == PaymentType.ORDER_PAYMENT || p.getType() == PaymentType.DIRECT_PAYMENT)
                .map(paymentMapper::toResponse)
                .toList();

        return new org.springframework.data.domain.PageImpl<>(filteredList, pageable, payments.getTotalElements());
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        Long userId = getCurrentUserId();
        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (!payment.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền xem giao dịch này");
        }

        return paymentMapper.toResponse(payment);
    }

    @Override
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepo.findAll(pageable);
        return payments.map(paymentMapper::toResponse);
    }

    @Override
    public Map<String, Object> getPaymentStatistics() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPayments", paymentRepo.count());
        stats.put("successfulPayments", paymentRepo.countByStatus(PaymentStatus.SUCCESS));
        stats.put("pendingPayments", paymentRepo.countByStatus(PaymentStatus.PENDING));
        stats.put("failedPayments", paymentRepo.countByStatus(PaymentStatus.FAILED));
        stats.put("monthlyRevenue", paymentRepo.getTotalSuccessAmountSince(startOfMonth));

        return stats;
    }

    @Override
    @Transactional
    public void cleanupExpiredPayments() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(30);
        List<Payment> expiredPayments = paymentRepo.findExpiredPendingPayments(expiredTime);

        for (Payment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setMessage("Hết hạn thanh toán");
            paymentRepo.save(payment);
        }
    }

    @Override
    @Transactional
    public PaymentResponse updateOrderStatus(String orderId, String newStatus) {
        Payment payment = paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + orderId));

        try {
            OrderStatus status = OrderStatus.valueOf(newStatus.toUpperCase());
            payment.setOrderStatus(status);
            paymentRepo.save(payment);

            Long buyerId = payment.getUser() != null ? payment.getUser().getId() : null;
            Long sellerId = payment.getBikePost() != null && payment.getBikePost().getUser() != null ? payment.getBikePost().getUser().getId() : null;
            realtimeNotificationService.notifyOrderStatusChange(buyerId, sellerId, payment.getId(), status.name());

            return paymentMapper.toResponse(payment);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Trạng thái đơn hàng không hợp lệ");
        }
    }


    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String reason, Long adminId) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Chỉ có thể hoàn tiền cho giao dịch thành công");
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new RuntimeException("Giao dịch này đã được hoàn tiền");
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundReason(reason);
        payment.setRefundedAt(LocalDateTime.now());

        Users admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin không tồn tại"));
        payment.setRefundedBy(admin);

        paymentRepo.save(payment);

        if (payment.getPointsEarned() != null && payment.getPointsEarned() > 0) {
            try {
                userService.addPoint(payment.getUser().getId(), -payment.getPointsEarned());
            } catch (Exception e) {
                log.error("Failed to deduct points for refund: paymentId={}", paymentId, e);
            }
        }

        notificationService.sendRefundNotificationEmail(payment);
        notificationService.sendRealTimeNotification(payment.getUser().getId(),
                "Giao dịch " + payment.getOrderId() + " đã được hoàn tiền.", "PAYMENT_REFUNDED");

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId, String reason) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy giao dịch đang chờ xử lý");
        }

        Long currentUserId = getCurrentUserId();
        if (!payment.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền hủy giao dịch này");
        }

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setMessage(reason);
        payment.setCompletedAt(LocalDateTime.now());

        paymentRepo.save(payment);

        notificationService.sendRealTimeNotification(payment.getUser().getId(),
                "Giao dịch " + payment.getOrderId() + " đã được hủy.", "PAYMENT_CANCELLED");

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public CreatePaymentResponse createOrderPaymentForNegotiation(Long buyerId, Long bikePostId, Long amount, String description) throws Exception {
        Users buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Buyer không tồn tại"));
        BikePost bikePost = bikePostRepository.findById(bikePostId)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        String orderId = "ORDER_" + System.currentTimeMillis();
        String desc = description != null ? description : "Thanh toan don hang " + orderId;

        Payment payment = Payment.builder()
                .user(buyer)
                .bikePost(bikePost)
                .orderId(orderId)
                .amount(amount)
                .description(desc)
                .status(PaymentStatus.PENDING)
                .type(PaymentType.ORDER_PAYMENT)
                .orderStatus(OrderStatus.PENDING_PAYMENT)
                .referenceId(bikePostId)
                .build();
        paymentRepo.save(payment);

        String vnpayUrl = generateVNPayUrl(orderId, amount);
        return CreatePaymentResponse.builder()
                .orderId(orderId)
                .amount(amount)
                .description(desc)
                .paymentUrl(vnpayUrl)
                .message("Tạo link thanh toán thành công")
                .success(true)
                .build();
    }

    @Override
    @Transactional
    public CreatePaymentResponse createInspectionPayment(Long sellerId, Long inspectionId, Double fee) throws Exception {
        Users seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Seller không tồn tại"));

        Long amount = fee.longValue();
        if (amount < 10000) amount = 10000L;

        String orderId = "INSP_" + System.currentTimeMillis();
        String desc = "Phi kiem dinh xe - ID " + inspectionId;

        Payment payment = Payment.builder()
                .user(seller)
                .orderId(orderId)
                .amount(amount)
                .description(desc)
                .status(PaymentStatus.PENDING)
                .type(PaymentType.INSPECTION_FEE)
                .referenceId(inspectionId)
                .build();
        paymentRepo.save(payment);

        String vnpayUrl = generateVNPayUrl(orderId, amount);
        return CreatePaymentResponse.builder()
                .orderId(orderId)
                .amount(amount)
                .description(desc)
                .paymentUrl(vnpayUrl)
                .message("Tạo link thanh toán phí kiểm định thành công")
                .success(true)
                .build();
    }

    @Override
    @Transactional
    public PaymentResponse submitDelivery(Long paymentId, Long sellerId, DeliveryUpdateRequest request) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (payment.getBikePost() == null || !payment.getBikePost().getUser().getId().equals(sellerId)) {
            throw new RuntimeException("Bạn không phải người bán của đơn hàng này");
        }

        if (payment.getOrderStatus() != OrderStatus.PAID_WAITING_DELIVERY) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ giao hàng");
        }

        payment.setDeliveryMethod(request.getDeliveryMethod());
        payment.setDeliveryEvidenceUrls(request.getDeliveryEvidenceUrls());
        payment.setOrderStatus(OrderStatus.IN_DELIVERY);
        paymentRepo.save(payment);

        Long buyerId = payment.getUser() != null ? payment.getUser().getId() : null;
        realtimeNotificationService.notifyOrderStatusChange(buyerId, sellerId, paymentId, OrderStatus.IN_DELIVERY.name());

        return paymentMapper.toResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepo.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch với mã: " + orderId));
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse confirmReceived(Long paymentId, Long buyerId) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (!payment.getUser().getId().equals(buyerId)) {
            throw new RuntimeException("Bạn không phải người mua của đơn hàng này");
        }

        if (payment.getOrderStatus() != OrderStatus.IN_DELIVERY) {
            throw new RuntimeException("Đơn hàng không ở trạng thái đang giao");
        }

        LocalDateTime now = LocalDateTime.now();
        payment.setOrderStatus(OrderStatus.DELIVERED);
        payment.setDeliveredAt(now);
        // autoReleaseAt chỉ dùng cho escrow online, không áp dụng cho COD
        if (payment.getType() != PaymentType.DIRECT_PAYMENT) {
            payment.setAutoReleaseAt(now.plusDays(7));
        }
        paymentRepo.save(payment);

        Long sellerId = payment.getBikePost() != null && payment.getBikePost().getUser() != null ? payment.getBikePost().getUser().getId() : null;
        realtimeNotificationService.notifyOrderStatusChange(buyerId, sellerId, paymentId, OrderStatus.DELIVERED.name());

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse cancelRequest(Long paymentId, Long buyerId, String reason) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (!payment.getUser().getId().equals(buyerId)) {
            throw new RuntimeException("Bạn không phải người mua của đơn hàng này");
        }

        if (payment.getOrderStatus() != OrderStatus.PAID_WAITING_DELIVERY) {
            throw new RuntimeException("Chỉ có thể yêu cầu hủy khi đơn hàng chưa được giao");
        }

        payment.setOrderStatus(OrderStatus.RETURN_REQUESTED);
        payment.setRefundReason(reason);
        paymentRepo.save(payment);

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse releaseEscrow(Long paymentId, Long adminId, String adminNote) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (payment.getReleasedAt() != null) {
            throw new RuntimeException("Escrow đã được xử lý cho đơn hàng này");
        }

        Long escrowAmt = payment.getEscrowPoints();
        if (escrowAmt == null || escrowAmt <= 0) {
            throw new RuntimeException("Không có điểm escrow để giải phóng");
        }

        if (payment.getBikePost() == null || payment.getBikePost().getUser() == null) {
            throw new RuntimeException("Không xác định được người bán");
        }

        Users seller = payment.getBikePost().getUser();
        long sellerBalanceAfter = seller.getPoint().longValue() + escrowAmt;
        seller.setPoint((int) sellerBalanceAfter);
        userRepository.save(seller);

        recordPointTransaction(seller, payment, PointTransactionType.ESCROW_RELEASE,
                escrowAmt, sellerBalanceAfter, "Giải phóng escrow cho người bán - đơn " + payment.getOrderId());

        payment.setEscrowPoints(0L);
        payment.setReleasedAt(LocalDateTime.now());
        payment.setOrderStatus(OrderStatus.COMPLETED);
        payment.setCompletedAt(LocalDateTime.now());
        payment.setAdminNote(adminNote);
        paymentRepo.save(payment);

        log.info("Released escrow with adminNote: {}", adminNote);

        if (adminNote != null && !adminNote.isEmpty()) {
            notificationService.sendRealTimeNotification(seller.getId(),
                "Admin đã giải phóng escrow: " + adminNote, "ESCROW_RELEASED");
        }

        log.info("Released {} escrow points to seller {} for payment {}", escrowAmt, seller.getId(), payment.getOrderId());
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refundEscrow(Long paymentId, Long adminId, String adminNote) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        if (payment.getReleasedAt() != null) {
            throw new RuntimeException("Escrow đã được xử lý cho đơn hàng này");
        }

        Long escrowAmt = payment.getEscrowPoints();
        if (escrowAmt == null || escrowAmt <= 0) {
            throw new RuntimeException("Không có điểm escrow để hoàn lại");
        }

        Users buyer = payment.getUser();
        long buyerBalanceAfter = buyer.getPoint().longValue() + escrowAmt;
        buyer.setPoint((int) buyerBalanceAfter);
        userRepository.save(buyer);

        recordPointTransaction(buyer, payment, PointTransactionType.ESCROW_REFUND,
                escrowAmt, buyerBalanceAfter, "Hoàn điểm escrow cho người mua - đơn " + payment.getOrderId());

        payment.setEscrowPoints(0L);
        payment.setReleasedAt(LocalDateTime.now());
        payment.setOrderStatus(OrderStatus.CANCELLED);
        payment.setAdminNote(adminNote);
        paymentRepo.save(payment);

        log.info("Refunded escrow with adminNote: {}", adminNote);

        if (adminNote != null && !adminNote.isEmpty()) {
            notificationService.sendRealTimeNotification(buyer.getId(),
                "Admin đã hoàn tiền: " + adminNote, "ESCROW_REFUNDED");
        }

        log.info("Refunded {} escrow points to buyer {} for payment {}", escrowAmt, buyer.getId(), payment.getOrderId());
        return paymentMapper.toResponse(payment);
    }

    @Override
    public String generateFreshPaymentUrl(String orderId, Long amount) throws Exception {
        return generateVNPayUrl(orderId, amount);
    }

    @Override
    @Transactional
    public PaymentResponse createDirectPayment(Long buyerId, Long bikePostId, String address, String description) {
        Users buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Người mua không tồn tại"));
        BikePost bikePost = bikePostRepository.findById(bikePostId)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        if (bikePost.getIsVerified()) {
            throw new RuntimeException("Xe đã kiểm định phải thanh toán online (escrow)");
        }

        Users seller = bikePost.getUserId() != null
                ? userRepository.findById(bikePost.getUserId()).orElse(null)
                : null;

        String orderId = "COD_" + System.currentTimeMillis();
        String desc = description != null ? description : "Mua xe COD - " + orderId;

        Payment payment = Payment.builder()
                .user(buyer)
                .seller(seller)
                .bikePost(bikePost)
                .orderId(orderId)
                .amount(bikePost.getPrice().longValue())
                .description(desc)
                .status(PaymentStatus.PENDING)
                .type(PaymentType.DIRECT_PAYMENT)
                .orderStatus(OrderStatus.PENDING_SELLER_CONFIRMATION)
                .address(address)
                .sellerConfirmationDeadline(LocalDateTime.now().plusMinutes(1))
                .build();

        paymentRepo.save(payment);

        // Notify seller
        if (seller != null) {
            realtimeNotificationService.notifyOrderStatusChange(
                    buyerId, seller.getId(), payment.getId(), OrderStatus.PENDING_SELLER_CONFIRMATION.name());
            notificationService.sendRealTimeNotification(seller.getId(),
                    "Bạn có đơn hàng COD mới cần xác nhận trong 24h! Mã đơn: " + orderId, "NEW_COD_ORDER");
        }

        log.info("Created DIRECT_PAYMENT order {} for bikePost {}", orderId, bikePostId);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse sellerConfirmOrder(Long paymentId, Long sellerId) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (payment.getBikePost() == null || !payment.getBikePost().getUserId().equals(sellerId)) {
            throw new RuntimeException("Bạn không phải người bán của đơn hàng này");
        }

        if (payment.getType() != PaymentType.DIRECT_PAYMENT) {
            throw new RuntimeException("Chỉ áp dụng cho đơn COD");
        }

        if (payment.getOrderStatus() != OrderStatus.PENDING_SELLER_CONFIRMATION) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ xác nhận");
        }

        // COD: không có tiền qua platform, seller tự giao + tự thu tiền mặt
        payment.setOrderStatus(OrderStatus.IN_DELIVERY);
        // PaymentStatus giữ PENDING — đây chỉ là record tracking, không phải giao dịch online

        // Mark post as sold
        BikePost post = payment.getBikePost();
        post.setPostStatus(PostStatus.SOLD);
        bikePostRepository.save(post);

        paymentRepo.save(payment);

        Long buyerId = payment.getUser() != null ? payment.getUser().getId() : null;
        realtimeNotificationService.notifyOrderStatusChange(buyerId, sellerId, paymentId, OrderStatus.IN_DELIVERY.name());
        notificationService.sendRealTimeNotification(buyerId,
                "Seller đã xác nhận đơn hàng " + payment.getOrderId() + "! Seller sẽ liên hệ và giao hàng trực tiếp, vui lòng chuẩn bị tiền mặt.", "COD_ORDER_CONFIRMED");

        log.info("Seller {} confirmed COD order {}", sellerId, payment.getOrderId());
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse sellerRejectOrder(Long paymentId, Long sellerId, String reason) {
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));

        if (payment.getBikePost() == null || !payment.getBikePost().getUserId().equals(sellerId)) {
            throw new RuntimeException("Bạn không phải người bán của đơn hàng này");
        }

        if (payment.getType() != PaymentType.DIRECT_PAYMENT) {
            throw new RuntimeException("Chỉ áp dụng cho đơn COD");
        }

        if (payment.getOrderStatus() != OrderStatus.PENDING_SELLER_CONFIRMATION) {
            throw new RuntimeException("Đơn hàng không ở trạng thái chờ xác nhận");
        }

        payment.setOrderStatus(OrderStatus.CANCELLED);
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setSellerRejectionReason(reason);

        // Kiểm tra số lần từ chối trong tháng
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        long rejectionCount = paymentRepo.countSellerRejectionsThisMonth(sellerId, startOfMonth);
        payment.setSellerRejectionCount((int) rejectionCount + 1);

        paymentRepo.save(payment);

        Long buyerId = payment.getUser() != null ? payment.getUser().getId() : null;
        notificationService.sendRealTimeNotification(buyerId,
                "Đơn hàng COD " + payment.getOrderId() + " đã bị từ chối. Lý do: " + reason, "COD_ORDER_REJECTED");

        // Cảnh báo seller nếu từ chối quá 3 lần trong tháng
        if (rejectionCount + 1 >= 3) {
            notificationService.sendRealTimeNotification(sellerId,
                    "Cảnh báo: Bạn đã từ chối " + (rejectionCount + 1) + " đơn hàng trong tháng này. Tài khoản có thể bị hạn chế.", "SELLER_REJECTION_WARNING");
            log.warn("Seller {} has rejected {} orders this month - threshold exceeded", sellerId, rejectionCount + 1);
        }

        log.info("Seller {} rejected COD order {} - reason: {}", sellerId, payment.getOrderId(), reason);
        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public void autoCancelExpiredSellerConfirmations() {
        List<Payment> expiredOrders = paymentRepo.findExpiredSellerConfirmations(LocalDateTime.now());

        for (Payment payment : expiredOrders) {
            payment.setOrderStatus(OrderStatus.CANCELLED);
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setMessage("Tự động hủy: Seller không phản hồi trong 24h");
            paymentRepo.save(payment);

            Long buyerId = payment.getUser() != null ? payment.getUser().getId() : null;
            Long sellerId = payment.getSeller() != null ? payment.getSeller().getId() : null;

            if (buyerId != null) {
                notificationService.sendRealTimeNotification(buyerId,
                        "Đơn hàng COD " + payment.getOrderId() + " đã bị tự động hủy vì seller không phản hồi trong 24h.", "COD_AUTO_CANCELLED");
            }
            if (sellerId != null) {
                notificationService.sendRealTimeNotification(sellerId,
                        "Đơn hàng COD " + payment.getOrderId() + " đã bị tự động hủy do không phản hồi đúng hạn.", "COD_AUTO_CANCELLED");
            }

            log.info("Auto-cancelled expired COD order: {}", payment.getOrderId());
        }

        if (!expiredOrders.isEmpty()) {
            log.info("Auto-cancelled {} expired COD seller confirmation(s)", expiredOrders.size());
        }
    }

    private void recordPointTransaction(Users user, Payment payment, PointTransactionType type,
                                        long delta, long balanceAfter, String note) {
        PointTransaction tx = PointTransaction.builder()
                .user(user)
                .payment(payment)
                .type(type)
                .pointsDelta(delta)
                .balanceAfter(balanceAfter)
                .note(note)
                .build();
        pointTransactionRepository.save(tx);

        realtimeNotificationService.notifyPointsChange(user.getId(), delta, balanceAfter, note);
    }

    private List<PaymentResponse> enrichWithHasRated(List<PaymentResponse> responses) {
        if (responses.isEmpty()) return responses;
        Set<Long> ids = responses.stream().map(PaymentResponse::getId).collect(Collectors.toSet());
        Set<Long> ratedIds = sellerRatingRepository.findRatedPaymentIds(ids);
        responses.forEach(r -> r.setHasRated(ratedIds.contains(r.getId())));
        return responses;
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return (long) user.getId();
        }
        throw new RuntimeException("Người dùng chưa đăng nhập");
    }

    private String generateVNPayUrl(String orderId, Long amount) throws Exception {
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpayTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100)); // VNPay uses cents
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang " + orderId);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpayReturnUrl);
        vnpParams.put("vnp_IpAddr", "127.0.0.1");
        vnpParams.put("vnp_CreateDate", new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date()));

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (String fieldName : fieldNames) {
            String fieldValue = vnpParams.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName).append("=").append(java.net.URLEncoder.encode(fieldValue, "UTF-8"));
                query.append(fieldName).append("=").append(java.net.URLEncoder.encode(fieldValue, "UTF-8"));
                if (fieldNames.indexOf(fieldName) < fieldNames.size() - 1) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }

        String vnpSecureHash = hmacSHA512(vnpayHashSecret, hashData.toString());
        query.append("&vnp_SecureHash=").append(vnpSecureHash);

        return vnpayApiUrl + "?" + query.toString();
    }

    private String hmacSHA512(String key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA512");
        mac.init(secretKeySpec);
        byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

        StringBuilder hex = new StringBuilder();
        for (byte b : rawHmac) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    @Override
    @Transactional
    public void handleVNPayReturn(Map<String, String> params) throws Exception {
        processVNPayLogic(params);
    }

    @Override
    @Transactional
    public void handleVNPayIPN(Map<String, String> params) throws Exception {
        processVNPayLogic(params);
    }

    private void processVNPayLogic(Map<String, String> params) {
        String orderId = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionNo = params.get("vnp_TransactionNo");

        if (orderId == null) return;

        Payment payment = paymentRepo.findByOrderId(orderId).orElse(null);
        if (payment == null || payment.getStatus() == PaymentStatus.SUCCESS) return;

        payment.setResponseCode(responseCode);
        if (transactionNo != null && !transactionNo.isEmpty()) {
            payment.setMomoTransId(transactionNo);
        }

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());

            // Only award points for non-escrow, non-package, non-inspection payments
            int points = 0;
            if (payment.getType() != null &&
                    payment.getType() != PaymentType.PRIORITY_PACKAGE &&
                    payment.getType() != PaymentType.INSPECTION_FEE &&
                    payment.getType() != PaymentType.ORDER_PAYMENT) {
                points = (int) (payment.getAmount() / 1000);
            }

            payment.setPointsEarned(points);
            paymentRepo.save(payment);

            if (points > 0) {
                userService.addPoint(payment.getUser().getId(), points);
            }

            if (payment.getType() != null && payment.getReferenceId() != null) {
                switch (payment.getType()) {
                    case PRIORITY_PACKAGE:
                        try {
                            PostPrioritySubscription sub = subscriptionRepository.findById(payment.getReferenceId()).orElseThrow();
                            sub.setIsActive(true);
                            sub.setStartDate(LocalDateTime.now());
                            sub.setEndDate(LocalDateTime.now().plusDays(sub.getPriorityPackage().getDurationDays()));
                            subscriptionRepository.save(sub);

                            BikePost post = sub.getPost();
                            post.setIsPriority(true);
                            bikePostRepository.save(post);
                        } catch (Exception e) {
                            log.error("Lỗi khi kích hoạt gói ưu tiên: ", e);
                        }
                        break;

                    case INSPECTION_FEE:
                        try {
                            com.example.cyclemartberemake.entity.Inspection inspection =
                                    inspectionRepository.findById(payment.getReferenceId()).orElseThrow();
                            inspection.setStatus(com.example.cyclemartberemake.entity.InspectionStatus.PENDING);
                            inspectionRepository.save(inspection);

                            BikePost post = inspection.getBikePost();
                            post.setIsRequestedInspection(true);
                            bikePostRepository.save(post);

                            log.info("Thanh toán phí kiểm định thành công, inspection ID: {} → PENDING", payment.getReferenceId());
                        } catch (Exception e) {
                            log.error("Lỗi khi kích hoạt kiểm định: ", e);
                        }
                        break;

                    case ORDER_PAYMENT:
                        try {
                            BikePost post = bikePostRepository.findById(payment.getReferenceId()).orElseThrow();
                            post.setPostStatus(PostStatus.SOLD);
                            bikePostRepository.save(post);

                            // Escrow: convert VND → points 1:1, hold in escrow (net 0 on buyer balance)
                            long escrowAmt = payment.getAmount();
                            Users buyer = payment.getUser();
                            long balanceBeforeTopup = buyer.getPoint().longValue();
                            long balanceAfterTopup = balanceBeforeTopup + escrowAmt;
                            long balanceAfterHold = balanceAfterTopup - escrowAmt; // same as before

                            recordPointTransaction(buyer, payment, PointTransactionType.TOP_UP_VNPAY,
                                    escrowAmt, balanceAfterTopup, "Nạp điểm từ VNPay - đơn " + payment.getOrderId());
                            recordPointTransaction(buyer, payment, PointTransactionType.ESCROW_HOLD,
                                    -escrowAmt, balanceAfterHold, "Giữ điểm escrow - đơn " + payment.getOrderId());
                            // buyer.point unchanged (net 0)

                            payment.setConvertedPoints(escrowAmt);
                            payment.setEscrowPoints(escrowAmt);
                            payment.setVerifiedAtPurchase(post.getIsVerified());
                            payment.setOrderStatus(OrderStatus.PAID_WAITING_DELIVERY);
                            paymentRepo.save(payment);

                            log.info("ORDER_PAYMENT success: escrowed {} points for payment {}", escrowAmt, payment.getOrderId());
                        } catch (Exception e) {
                            log.error("Lỗi khi xử lý escrow đơn hàng: ", e);
                        }
                        break;

                    case ORDER_DEPOSIT:
                        break;

                    default:
                        break;
                }
            }

            try {
                notificationService.sendPaymentSuccessEmail(payment);

                // Cập nhật câu thông báo gửi đến Client
                String notifyMsg = "Thanh toán thành công!";
                if (points > 0) {
                    notifyMsg += " Bạn đã được cộng " + points + " điểm.";
                }

                notificationService.sendRealTimeNotification(payment.getUser().getId(), notifyMsg, "PAYMENT_SUCCESS");
            } catch(Exception ignored) {}

        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepo.save(payment);

            try {
                notificationService.sendPaymentFailedEmail(payment);
                notificationService.sendRealTimeNotification(payment.getUser().getId(),
                        "Thanh toán thất bại. Vui lòng thử lại.", "PAYMENT_FAILED");
            } catch(Exception ignored) {}



        }
    }
}
