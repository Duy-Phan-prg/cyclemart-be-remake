package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.CreatePaymentRequest;
import com.example.cyclemartberemake.dto.response.CreatePaymentResponse;
import com.example.cyclemartberemake.dto.response.PaymentResponse;
import com.example.cyclemartberemake.entity.Payment;
import com.example.cyclemartberemake.entity.PaymentStatus;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.PaymentMapper;
import com.example.cyclemartberemake.repository.PaymentRepository;
import com.example.cyclemartberemake.service.PaymentNotificationService;
import com.example.cyclemartberemake.service.PaymentService;
import com.example.cyclemartberemake.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepo;
    private final UserService userService;
    private final PaymentMapper paymentMapper;
    private final PaymentNotificationService notificationService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.returnUrl}")
    private String returnUrl;

    @Value("${momo.ipnUrl}")
    private String ipnUrl;

    @Override
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) throws Exception {
        
        // 🔥 Lấy user từ JWT
        Long userId = getCurrentUserId();
        
        // 🔥 Validate amount
        if (request.getAmount() < 10000 || request.getAmount() > 50000000) {
            throw new RuntimeException("Số tiền không hợp lệ. Phải từ 10,000 - 50,000,000 VND");
        }

        String orderId = "ORDER_" + System.currentTimeMillis();
        String requestId = orderId;
        String description = request.getDescription() != null ? 
            request.getDescription() : "Nạp điểm CycleMart";

        // 🔥 Lưu payment record
        Payment payment = Payment.builder()
                .userId(userId)
                .orderId(orderId)
                .amount(request.getAmount())
                .description(description)
                .status(PaymentStatus.PENDING)
                .city(request.getCity())
                .district(request.getDistrict())
                .ipAddress(request.getIpAddress())
                .build();

        paymentRepo.save(payment);
        log.info("Created payment record: orderId={}, userId={}, amount={}", orderId, userId, request.getAmount());

        try {
            // 🔥 Tạo signature với đầy đủ validation
            String extraData = ""; // 🔥 MoMo yêu cầu extraData không được null
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + request.getAmount() +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + ipnUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + description +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";

            String signature = hmacSHA256(rawHash, secretKey);
            payment.setSignature(signature);
            paymentRepo.save(payment);

            // 🔥 Gọi MoMo API
            Map<String, Object> body = new HashMap<>();
            body.put("partnerCode", partnerCode);
            body.put("requestId", requestId);
            body.put("amount", request.getAmount());
            body.put("orderId", orderId);
            body.put("orderInfo", description);
            body.put("redirectUrl", returnUrl);
            body.put("ipnUrl", ipnUrl);
            body.put("extraData", extraData); // 🔥 Thêm extraData
            body.put("requestType", "captureWallet");
            body.put("signature", signature);
            body.put("lang", "vi");

            Map<String, Object> response = restTemplate.postForObject(endpoint, body, Map.class);
            
            if (response != null && "0".equals(response.get("resultCode").toString())) {
                log.info("MoMo payment created successfully: orderId={}", orderId);
                
                return CreatePaymentResponse.builder()
                        .orderId(orderId)
                        .amount(request.getAmount())
                        .payUrl(response.get("payUrl").toString())
                        .qrCodeUrl(response.get("qrCodeUrl") != null ? response.get("qrCodeUrl").toString() : null)
                        .deeplink(response.get("deeplink") != null ? response.get("deeplink").toString() : null)
                        .message("Tạo thanh toán thành công")
                        .success(true)
                        .build();
            } else {
                // 🔥 MoMo API error
                String errorMsg = response != null ? response.get("message").toString() : "Lỗi không xác định";
                payment.setStatus(PaymentStatus.FAILED);
                payment.setMessage(errorMsg);
                paymentRepo.save(payment);
                
                log.error("MoMo API error: orderId={}, error={}", orderId, errorMsg);
                throw new RuntimeException("Không thể tạo thanh toán: " + errorMsg);
            }

        } catch (Exception e) {
            // 🔥 Error handling
            payment.setStatus(PaymentStatus.FAILED);
            payment.setMessage("Lỗi hệ thống: " + e.getMessage());
            paymentRepo.save(payment);
            
            log.error("Payment creation failed: orderId={}, error={}", orderId, e.getMessage(), e);
            throw new RuntimeException("Lỗi tạo thanh toán: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handleIPN(Map<String, Object> data) {
        
        String orderId = data.get("orderId").toString();
        String resultCode = data.get("resultCode").toString();
        String signature = data.get("signature").toString();
        
        log.info("Received IPN: orderId={}, resultCode={}", orderId, resultCode);

        // 🔥 Tìm payment record
        Payment payment = paymentRepo.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            log.error("Payment not found for orderId: {}", orderId);
            return;
        }

        // 🔥 Kiểm tra đã xử lý chưa
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("Payment already processed: orderId={}", orderId);
            return;
        }

        // 🔥 Verify signature để đảm bảo request từ MoMo
        try {
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + data.get("amount") +
                    "&extraData=" + data.getOrDefault("extraData", "") +
                    "&message=" + data.get("message") +
                    "&orderId=" + orderId +
                    "&orderInfo=" + data.get("orderInfo") +
                    "&orderType=" + data.get("orderType") +
                    "&partnerCode=" + partnerCode +
                    "&payType=" + data.get("payType") +
                    "&requestId=" + data.get("requestId") +
                    "&responseTime=" + data.get("responseTime") +
                    "&resultCode=" + resultCode +
                    "&transId=" + data.get("transId");

            String expectedSignature = hmacSHA256(rawHash, secretKey);
            
            if (!expectedSignature.equals(signature)) {
                log.error("Invalid signature for orderId: {}", orderId);
                return;
            }

        } catch (Exception e) {
            log.error("Error verifying signature for orderId: {}", orderId, e);
            return;
        }

        // 🔥 Cập nhật payment status
        payment.setMomoTransId(data.get("transId").toString());
        payment.setResponseCode(resultCode);
        payment.setMessage(data.get("message").toString());
        payment.setCompletedAt(LocalDateTime.now());

        if ("0".equals(resultCode)) {
            // 🔥 Thanh toán thành công
            payment.setStatus(PaymentStatus.SUCCESS);
            
            // 🔥 Tính điểm: 1000 VND = 1 điểm
            int points = (int) (payment.getAmount() / 1000);
            payment.setPointsEarned(points);
            
            paymentRepo.save(payment);
            
            // 🔥 Cộng điểm cho user
            userService.addPoint(payment.getUserId(), points);
            
            log.info("Payment successful: orderId={}, points={}", orderId, points);
            
            // 🔥 Send success notification
            notificationService.sendPaymentSuccessEmail(payment);
            notificationService.sendRealTimeNotification(payment.getUserId(), 
                "Thanh toán thành công! Bạn đã được cộng " + points + " điểm.", "PAYMENT_SUCCESS");
            
        } else {
            // 🔥 Thanh toán thất bại
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(payment);
            
            log.warn("Payment failed: orderId={}, resultCode={}", orderId, resultCode);
            
            // 🔥 Send failure notification
            notificationService.sendPaymentFailedEmail(payment);
            notificationService.sendRealTimeNotification(payment.getUserId(), 
                "Thanh toán thất bại. Vui lòng thử lại.", "PAYMENT_FAILED");
        }
    }

    @Override
    public Page<PaymentResponse> getPaymentHistory(Pageable pageable) {
        Long userId = getCurrentUserId();
        Page<Payment> payments = paymentRepo.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return payments.map(paymentMapper::toResponse);
    }

    @Override
    public Page<PaymentResponse> getPaymentHistoryByStatus(String status, Pageable pageable) {
        Long userId = getCurrentUserId();
        PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        Page<Payment> payments = paymentRepo.findByUserIdAndStatusOrderByCreatedAtDesc(userId, paymentStatus, pageable);
        return payments.map(paymentMapper::toResponse);
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        Long userId = getCurrentUserId();
        Payment payment = paymentRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));
        
        // 🔥 Kiểm tra ownership
        if (!payment.getUserId().equals(userId)) {
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
        // 🔥 Tìm payment pending quá 30 phút
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(30);
        List<Payment> expiredPayments = paymentRepo.findExpiredPendingPayments(expiredTime);
        
        for (Payment payment : expiredPayments) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setMessage("Hết hạn thanh toán");
            paymentRepo.save(payment);
        }
        
        log.info("Cleaned up {} expired payments", expiredPayments.size());
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId, String reason, Long adminId) {
        // 🔥 Tìm payment
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        // 🔥 Validate payment có thể refund không
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Chỉ có thể hoàn tiền cho giao dịch thành công");
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new RuntimeException("Giao dịch này đã được hoàn tiền");
        }

        // 🔥 Cập nhật payment status
        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setRefundReason(reason);
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundedBy(adminId);

        paymentRepo.save(payment);

        // 🔥 Trừ điểm đã cộng (nếu có)
        if (payment.getPointsEarned() != null && payment.getPointsEarned() > 0) {
            try {
                userService.addPoint(payment.getUserId(), -payment.getPointsEarned());
                log.info("Deducted {} points from user {} for refund", payment.getPointsEarned(), payment.getUserId());
            } catch (Exception e) {
                log.error("Failed to deduct points for refund: paymentId={}", paymentId, e);
            }
        }

        // 🔥 Send refund notification
        notificationService.sendRefundNotificationEmail(payment);
        notificationService.sendRealTimeNotification(payment.getUserId(), 
            "Giao dịch " + payment.getOrderId() + " đã được hoàn tiền.", "PAYMENT_REFUNDED");

        log.info("Payment refunded successfully: paymentId={}, orderId={}, reason={}", 
                paymentId, payment.getOrderId(), reason);

        return paymentMapper.toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId, String reason) {
        // 🔥 Tìm payment
        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch"));

        // 🔥 Validate payment có thể cancel không
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể hủy giao dịch đang chờ xử lý");
        }

        // 🔥 Kiểm tra ownership (user chỉ có thể cancel payment của mình)
        Long currentUserId = getCurrentUserId();
        if (!payment.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền hủy giao dịch này");
        }

        // 🔥 Cập nhật payment status
        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setMessage(reason);
        payment.setCompletedAt(LocalDateTime.now());

        paymentRepo.save(payment);

        // 🔥 Send cancellation notification
        notificationService.sendRealTimeNotification(payment.getUserId(), 
            "Giao dịch " + payment.getOrderId() + " đã được hủy.", "PAYMENT_CANCELLED");

        log.info("Payment cancelled: paymentId={}, orderId={}, reason={}", 
                paymentId, payment.getOrderId(), reason);

        return paymentMapper.toResponse(payment);
    }

    // 🔥 Helper methods
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return (long) user.getId();
        }
        throw new RuntimeException("Người dùng chưa đăng nhập");
    }

    private String hmacSHA256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        mac.init(secretKeySpec);

        byte[] rawHmac = mac.doFinal(data.getBytes());

        StringBuilder hex = new StringBuilder();
        for (byte b : rawHmac) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }
}