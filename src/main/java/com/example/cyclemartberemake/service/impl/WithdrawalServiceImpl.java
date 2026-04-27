package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.WithdrawalRequestDTO;
import com.example.cyclemartberemake.dto.response.WithdrawalResponse;
import com.example.cyclemartberemake.entity.*;
import com.example.cyclemartberemake.repository.PointTransactionRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.repository.WithdrawalRequestRepository;
import com.example.cyclemartberemake.service.WithdrawalService;
import com.example.cyclemartberemake.service.RealtimeNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private final WithdrawalRequestRepository withdrawalRepo;
    private final UserRepository userRepository;
    private final PointTransactionRepository pointTransactionRepository;
    private final RealtimeNotificationService realtimeNotificationService;

    @Override
    @Transactional
    public WithdrawalResponse createRequest(Long userId, WithdrawalRequestDTO dto) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (user.getPoint() == null || user.getPoint() < dto.getAmount()) {
            throw new RuntimeException("Số điểm không đủ để rút. Bạn hiện có " + (user.getPoint() == null ? 0 : user.getPoint()) + " điểm.");
        }

        // Trừ điểm ngay khi tạo request
        long balanceBefore = user.getPoint();
        user.setPoint((int) (user.getPoint() - dto.getAmount()));
        userRepository.save(user);

        pointTransactionRepository.save(PointTransaction.builder()
                .user(user)
                .type(PointTransactionType.WITHDRAW_REQUEST)
                .pointsDelta(-dto.getAmount())
                .balanceAfter((long) user.getPoint())
                .note("Yêu cầu rút " + dto.getAmount() + " điểm về TK " + dto.getBankName() + " - " + dto.getAccountNumber())
                .build());

        realtimeNotificationService.notifyPointsChange(userId, -dto.getAmount(), (long) user.getPoint(),
            "Yêu cầu rút " + dto.getAmount() + " điểm");

        WithdrawalRequest request = WithdrawalRequest.builder()
                .user(user)
                .amount(dto.getAmount())
                .bankName(dto.getBankName())
                .accountNumber(dto.getAccountNumber())
                .accountHolder(dto.getAccountHolder())
                .status(WithdrawalStatus.PENDING)
                .build();

        return toResponse(withdrawalRepo.save(request));
    }

    @Override
    public Page<WithdrawalResponse> getMyRequests(Long userId, Pageable pageable) {
        return withdrawalRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    @Override
    public Page<WithdrawalResponse> getAllForAdmin(Pageable pageable) {
        return withdrawalRepo.findAllByOrderByCreatedAtDesc(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public WithdrawalResponse complete(Long requestId, Long adminId, String note) {
        WithdrawalRequest request = getAndValidatePending(requestId);
        Users admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin không tồn tại"));

        request.setStatus(WithdrawalStatus.COMPLETED);
        request.setAdminNote(note);
        request.setProcessedBy(admin);
        request.setProcessedAt(LocalDateTime.now());

        pointTransactionRepository.save(PointTransaction.builder()
                .user(request.getUser())
                .type(PointTransactionType.WITHDRAW_APPROVED)
                .pointsDelta(0L)
                .balanceAfter((long) request.getUser().getPoint())
                .note("Admin đã chuyển khoản " + request.getAmount() + " VND - Yêu cầu #" + requestId)
                .build());

        realtimeNotificationService.notifyPointsChange(request.getUser().getId(), 0L, (long) request.getUser().getPoint(),
            "Yêu cầu rút tiền đã được duyệt");

        return toResponse(withdrawalRepo.save(request));
    }

    @Override
    @Transactional
    public WithdrawalResponse reject(Long requestId, Long adminId, String note) {
        WithdrawalRequest request = getAndValidatePending(requestId);
        Users admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Admin không tồn tại"));

        Users user = request.getUser();
        user.setPoint((int) (user.getPoint() + request.getAmount()));
        userRepository.save(user);

        pointTransactionRepository.save(PointTransaction.builder()
                .user(user)
                .type(PointTransactionType.WITHDRAW_REJECTED)
                .pointsDelta(request.getAmount())
                .balanceAfter((long) user.getPoint())
                .note("Hoàn điểm do yêu cầu rút #" + requestId + " bị từ chối" + (note != null ? ": " + note : ""))
                .build());

        realtimeNotificationService.notifyPointsChange(user.getId(), request.getAmount(), (long) user.getPoint(),
            "Yêu cầu rút tiền bị từ chối - Đã hoàn điểm");

        request.setStatus(WithdrawalStatus.REJECTED);
        request.setAdminNote(note);
        request.setProcessedBy(admin);
        request.setProcessedAt(LocalDateTime.now());

        return toResponse(withdrawalRepo.save(request));
    }

    private WithdrawalRequest getAndValidatePending(Long requestId) {
        WithdrawalRequest request = withdrawalRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Yêu cầu rút tiền không tồn tại"));
        if (request.getStatus() != WithdrawalStatus.PENDING) {
            throw new RuntimeException("Yêu cầu này đã được xử lý");
        }
        return request;
    }

    private WithdrawalResponse toResponse(WithdrawalRequest r) {
        return WithdrawalResponse.builder()
                .id(r.getId())
                .userId(r.getUser().getId())
                .userName(r.getUser().getFullName())
                .amount(r.getAmount())
                .bankName(r.getBankName())
                .accountNumber(r.getAccountNumber())
                .accountHolder(r.getAccountHolder())
                .status(r.getStatus().name())
                .adminNote(r.getAdminNote())
                .processedByName(r.getProcessedBy() != null ? r.getProcessedBy().getFullName() : null)
                .processedAt(r.getProcessedAt())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
