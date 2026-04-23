package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.CreatePaymentRequest;
import com.example.cyclemartberemake.dto.response.CreatePaymentResponse;
import com.example.cyclemartberemake.dto.response.PaymentResponse;
import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.Payment;
import com.example.cyclemartberemake.entity.PaymentStatus;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.PaymentMapper;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.PaymentRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.PaymentNotificationService;
import com.example.cyclemartberemake.service.PaymentService;
import com.example.cyclemartberemake.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    private final UserRepository userRepository;
    private final BikePostRepository bikePostRepository;
    private final UserService userService;
    private final PaymentMapper paymentMapper;
    private final PaymentNotificationService notificationService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${sepay.merchantId}")
    private String merchantId;

    @Value("${sepay.secretKey}")
    private String secretKey;

    @Value("${sepay.apiUrl}")
    private String apiUrl;

    @Value("${sepay.returnUrl}")
    private String returnUrl;

    @Value("${sepay.cancelUrl}")
    private String cancelUrl;

    @Value("${sepay.ipnUrl}")
    private String ipnUrl;

    @Value("${sepay.bankAccount}")
    private String bankAccount;

    @Value("${sepay.bankCode}")
    private String bankCode;

    @Override
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) throws Exception {

        Long userId = getCurrentUserId();

        // Lấy BikePost để get price
        BikePost bikePost = bikePostRepository.findById(request.getBikePostId())
            .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));
        
        Long amount = bikePost.getPrice().longValue();

        if (amount < 10000 || amount > 50000000) {
            throw new RuntimeException("Số tiền không hợp lệ. Phải từ 10,000 - 50,000,000 VND");
        }

        String orderId = "ORDER_" + System.currentTimeMillis();
        String description = request.getDescription() != null ? 
            request.getDescription().replaceAll("[^a-zA-Z0-9\\s]", "") : "Nap diem CycleMart";

        Payment payment = Payment.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")))
                .bikePost(bikePost)
                .orderId(orderId)
                .amount(amount)
                .description(description)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepo.save(payment);
        log.info("Created payment record: orderId={}, userId={}, amount={}", orderId, userId, amount);

        try {
            // 🔥 TẠO QR CODE CHO CHUYỂN KHOẢN
            // Format: https://qr.sepay.vn/img?acc=ACCOUNT&bank=BANK_CODE&amount=AMOUNT&des=DESCRIPTION
            
            String qrUrl = String.format(
                "https://qr.sepay.vn/img?acc=%s&bank=%s&amount=%d&des=%s",
                bankAccount,
                bankCode,
                amount,
                orderId
            );
            
            log.info("Generated QR URL: {}", qrUrl);
            
            return CreatePaymentResponse.builder()
                    .orderId(orderId)
                    .amount(amount)
                    .description(description)
                    .qrUrl(qrUrl)
                    .message("Tạo mã QR thành công. Vui lòng quét QR để chuyển khoản")
                    .success(true)
                    .build();

        } catch (Exception e) {
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

        Map<String, Object> order = (Map<String, Object>) data.get("order");

        String orderId = null;
        if (order != null) {
            orderId = (String) order.get("order_id");
        }

        // fallback
        if (orderId == null) {
            orderId = (String) data.get("order_id");
        }
        if (orderId == null) {
            orderId = (String) data.get("orderCode");
        }

        String status = (String) data.get("notification_type");
        String signature = (String) data.get("signature");
        
        System.out.println("=== handleIPN: orderId=" + orderId + ", status=" + status);
        
        log.info("Received Sepay IPN: orderId={}, status={}", orderId, status);

        if (orderId == null) {
            System.out.println("=== ERROR: orderId is null");
            log.error("Order ID is null in IPN data");
            return;
        }

        Payment payment = paymentRepo.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            System.out.println("=== ERROR: Payment not found for orderId: " + orderId);
            log.error("Payment not found for orderId: {}", orderId);
            return;
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            System.out.println("=== Payment already processed: " + orderId);
            log.warn("Payment already processed: orderId={}", orderId);
            return;
        }

        try {
            // Validate signature (nếu có)
            if (signature != null) {
                String expectedSignature = generateSepaySignature(data);
                if (!expectedSignature.equals(signature)) {
                    System.out.println("=== ERROR: Invalid signature");
                    log.error("Invalid signature for orderId: {}", orderId);
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("=== ERROR: Signature validation failed: " + e.getMessage());
            log.error("Error verifying signature for orderId: {}", orderId, e);
            return;
        }

        payment.setResponseCode(status);
        payment.setCompletedAt(LocalDateTime.now());

        // Sepay gửi notification_type = PAYMENT_SUCCESS
        if ("PAYMENT_SUCCESS".equals(status)) {
            payment.setStatus(PaymentStatus.SUCCESS);

            int points = (int) (payment.getAmount() / 1000);
            payment.setPointsEarned(points);
            
            paymentRepo.save(payment);

            userService.addPoint(payment.getUser().getId(), points);
            
            System.out.println("=== Payment SUCCESS: orderId=" + orderId + ", points=" + points);
            log.info("Payment successful: orderId={}, points={}", orderId, points);

            notificationService.sendPaymentSuccessEmail(payment);
            notificationService.sendRealTimeNotification(payment.getUser().getId(), 
                "Thanh toán thành công! Bạn đã được cộng " + points + " điểm.", "PAYMENT_SUCCESS");
            
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(payment);
            
            System.out.println("=== Payment FAILED: orderId=" + orderId + ", status=" + status);
            log.warn("Payment failed: orderId={}, status={}", orderId, status);

            notificationService.sendPaymentFailedEmail(payment);
            notificationService.sendRealTimeNotification(payment.getUser().getId(), 
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
        
        log.info("Cleaned up {} expired payments", expiredPayments.size());
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
                log.info("Deducted {} points from user {} for refund", payment.getPointsEarned(), payment.getUser().getId());
            } catch (Exception e) {
                log.error("Failed to deduct points for refund: paymentId={}", paymentId, e);
            }
        }

        notificationService.sendRefundNotificationEmail(payment);
        notificationService.sendRealTimeNotification(payment.getUser().getId(), 
            "Giao dịch " + payment.getOrderId() + " đã được hoàn tiền.", "PAYMENT_REFUNDED");

        log.info("Payment refunded successfully: paymentId={}, orderId={}, reason={}", 
                paymentId, payment.getOrderId(), reason);

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

        log.info("Payment cancelled: paymentId={}, orderId={}, reason={}", 
                paymentId, payment.getOrderId(), reason);

        return paymentMapper.toResponse(payment);
    }


    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return (long) user.getId();
        }
        throw new RuntimeException("Người dùng chưa đăng nhập");
    }

    private String generateSepaySignature(Map<String, Object> data) throws Exception {
        // Sepay signature: HMAC-SHA256 của sorted data
        StringBuilder sb = new StringBuilder();
        data.keySet().stream()
            .filter(key -> !"signature".equals(key))
            .sorted()
            .forEach(key -> {
                Object value = data.get(key);
                if (value != null) {
                    sb.append(key).append("=").append(value).append("&");
                }
            });
        
        String dataToSign = sb.toString();
        if (dataToSign.endsWith("&")) {
            dataToSign = dataToSign.substring(0, dataToSign.length() - 1);
        }
        
        return hmacSHA256(dataToSign, secretKey);
    }

    private String hmacSHA256(String data, String key) throws Exception {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

            StringBuilder hex = new StringBuilder();
            for (byte b : rawHmac) {
                hex.append(String.format("%02x", b));
            }

            String signature = hex.toString();
            log.info("Generated signature: {}", signature);
            return signature;
            
        } catch (Exception e) {
            log.error("Error generating HMAC signature: {}", e.getMessage(), e);
            throw e;
        }
    }
}