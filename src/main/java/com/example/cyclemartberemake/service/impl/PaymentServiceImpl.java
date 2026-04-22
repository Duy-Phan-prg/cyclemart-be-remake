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

        Long userId = getCurrentUserId();

        // Lấy BikePost để get price
        BikePost bikePost = bikePostRepository.findById(request.getBikePostId())
            .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));
        
        Long amount = bikePost.getPrice().longValue();

        if (amount < 10000 || amount > 50000000) {
            throw new RuntimeException("Số tiền không hợp lệ. Phải từ 10,000 - 50,000,000 VND");
        }

        String orderId = "ORDER_" + System.currentTimeMillis();
        String requestId = orderId;
        String description = request.getDescription() != null ? 
            request.getDescription().replaceAll("[^a-zA-Z0-9\\s]", "") : "Nap diem CycleMart";
        
        // Ensure description is safe for MoMo (no Vietnamese characters or special chars)
        description = description.replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                                .replaceAll("[ìíịỉĩ]", "i")
                                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                                .replaceAll("[ùúụủũưừứựửữ]", "u")
                                .replaceAll("[ỳýỵỷỹ]", "y")
                                .replaceAll("[đ]", "d")
                                .replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A")
                                .replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E")
                                .replaceAll("[ÌÍỊỈĨ]", "I")
                                .replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O")
                                .replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U")
                                .replaceAll("[ỲÝỴỶỸ]", "Y")
                                .replaceAll("[Đ]", "D");


        Payment payment = Payment.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")))
                .orderId(orderId)
                .amount(amount)
                .description(description)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepo.save(payment);
        log.info("Created payment record: orderId={}, userId={}, amount={}", orderId, userId, amount);

        try {
            // 🔥 GỌI MOMO API với credentials mới
            String extraData = "";
            String amountStr = amount.toString();
            String rawHash = "accessKey=" + accessKey +
                    "&amount=" + amountStr +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + ipnUrl +
                    "&orderId=" + orderId +
                    "&orderInfo=" + description +
                    "&partnerCode=" + partnerCode +
                    "&redirectUrl=" + returnUrl +
                    "&requestId=" + requestId +
                    "&requestType=captureWallet";

            log.info("MoMo signature raw hash: {}", rawHash);
            String signature = hmacSHA256(rawHash, secretKey);
            log.info("MoMo signature generated: {}", signature);
            
            payment.setSignature(signature);
            paymentRepo.save(payment);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            Map<String, Object> body = new HashMap<>();
            body.put("partnerCode", partnerCode);
            body.put("accessKey", accessKey);
            body.put("requestId", requestId);
            body.put("amount", amountStr);
            body.put("orderId", orderId);
            body.put("orderInfo", description);
            body.put("redirectUrl", returnUrl);
            body.put("ipnUrl", ipnUrl);
            body.put("extraData", extraData);
            body.put("requestType", "captureWallet");
            body.put("signature", signature);
            body.put("lang", "vi");

            log.info("MoMo request body: {}", body);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(endpoint, entity, Map.class);
            Map<String, Object> response = responseEntity.getBody();
            
            log.info("MoMo response: {}", response);
            
            if (response != null && "0".equals(response.get("resultCode").toString())) {
                log.info("MoMo payment created successfully: orderId={}", orderId);
                
                return CreatePaymentResponse.builder()
                        .orderId(orderId)
                        .amount(amount)
                        .description(description)
                        .payUrl(response.get("payUrl") != null ? response.get("payUrl").toString() : null)
                        .qrCodeUrl(response.get("qrCodeUrl") != null ? response.get("qrCodeUrl").toString() : null)
                        .deeplink(response.get("deeplink") != null ? response.get("deeplink").toString() : null)
                        .message("Tạo thanh toán thành công")
                        .success(true)
                        .build();
            } else {
                // 🔥 MoMo API error - Log detailed error info
                String resultCode = response != null ? response.get("resultCode").toString() : "unknown";
                String errorMsg = response != null ? response.get("message").toString() : "Lỗi không xác định";
                
                payment.setStatus(PaymentStatus.FAILED);
                payment.setMessage(errorMsg);
                paymentRepo.save(payment);
                
                log.error("MoMo API error: orderId={}, resultCode={}, error={}", orderId, resultCode, errorMsg);
                throw new RuntimeException("Không thể tạo thanh toán MoMo (Code: " + resultCode + "): " + errorMsg);
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

        Payment payment = paymentRepo.findByOrderId(orderId).orElse(null);
        if (payment == null) {
            log.error("Payment not found for orderId: {}", orderId);
            return;
        }

        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            log.warn("Payment already processed: orderId={}", orderId);
            return;
        }

        try {
            // 🔥 Validate signature với credentials mới
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

        payment.setMomoTransId(data.get("transId").toString());
        payment.setResponseCode(resultCode);
        payment.setMessage(data.get("message").toString());
        payment.setCompletedAt(LocalDateTime.now());

        if ("0".equals(resultCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);

            int points = (int) (payment.getAmount() / 1000);
            payment.setPointsEarned(points);
            
            paymentRepo.save(payment);

            userService.addPoint(payment.getUser().getId(), points);
            
            log.info("Payment successful: orderId={}, points={}", orderId, points);

            notificationService.sendPaymentSuccessEmail(payment);
            notificationService.sendRealTimeNotification(payment.getUser().getId(), 
                "Thanh toán thành công! Bạn đã được cộng " + points + " điểm.", "PAYMENT_SUCCESS");
            
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepo.save(payment);
            
            log.warn("Payment failed: orderId={}, resultCode={}", orderId, resultCode);

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

    private String hmacSHA256(String data, String key) throws Exception {
        try {
            // 🔥 Ensure UTF-8 encoding
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] rawHmac = mac.doFinal(data.getBytes("UTF-8"));

            // 🔥 Convert to lowercase hex (MoMo requirement)
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