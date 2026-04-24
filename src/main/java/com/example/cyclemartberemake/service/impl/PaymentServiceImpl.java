package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.CreatePaymentRequest;
import com.example.cyclemartberemake.dto.response.CreatePaymentResponse;
import com.example.cyclemartberemake.dto.response.PaymentResponse;
import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.Payment;
import com.example.cyclemartberemake.entity.PaymentStatus;
import com.example.cyclemartberemake.entity.PaymentType; // 🔥 Import Enum
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.entity.PostPrioritySubscription;
import com.example.cyclemartberemake.mapper.PaymentMapper;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.PaymentRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.repository.PostPrioritySubscriptionRepository;
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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
    private final PostPrioritySubscriptionRepository subscriptionRepository;

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

        // 1. Chuyển String type từ FE gửi lên thành Enum, mặc định là OTHER
        PaymentType paymentType = PaymentType.OTHER;
        if (request.getType() != null) {
            try {
                paymentType = PaymentType.valueOf(request.getType().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Loại thanh toán không hợp lệ: {}", request.getType());
            }
        }

        BikePost bikePost = null;
        // 2. Chỉ gán bikePost vào hóa đơn nếu KHÔNG PHẢI Mua Gói (để tránh lỗi hiển thị mua xe)
        if (request.getBikePostId() != null && paymentType != PaymentType.PRIORITY_PACKAGE) {
            bikePost = bikePostRepository.findById(request.getBikePostId())
                    .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));
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

        Payment payment = Payment.builder()
                .user(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")))
                .bikePost(bikePost) // Có thể null nếu mua gói
                .orderId(orderId)
                .amount(amount)
                .description(description)
                .status(PaymentStatus.PENDING)
                .type(paymentType) // 🔥 Đã thêm: Lưu loại giao dịch
                .referenceId(request.getReferenceId()) // 🔥 Đã thêm: Lưu ID tương ứng (ID gói, ID kiểm định...)
                .build();

        paymentRepo.save(payment);
        log.info("Created payment record: orderId={}, userId={}, amount={}, type={}", orderId, userId, amount, paymentType);

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
        String transactionNo = params.get("vnp_TransactionNo"); // Lấy mã giao dịch VNPay

        log.info("VNPay Callback: orderId={}, responseCode={}", orderId, responseCode);

        if (orderId == null) throw new RuntimeException("Order ID is null");

        Payment payment = paymentRepo.findByOrderId(orderId).orElse(null);
        if (payment == null || payment.getStatus() == PaymentStatus.SUCCESS) return;

        // Lưu thông tin VNPay trả về
        payment.setResponseCode(responseCode);
        if (transactionNo != null && !transactionNo.isEmpty()) {
            payment.setMomoTransId(transactionNo); // Re-use trường này hoặc tạo trường vnpayTransId mới
        }

        if ("00".equals(responseCode)) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setCompletedAt(LocalDateTime.now());
            int points = (int) (payment.getAmount() / 1000);
            payment.setPointsEarned(points);

            paymentRepo.save(payment);
            userService.addPoint(payment.getUser().getId(), points);

            log.info("VNPay payment successful: orderId={}, points={}", orderId, points);

            // 🔥 SỬ DỤNG SWITCH-CASE ĐỂ KÍCH HOẠT DỊCH VỤ DỰA TRÊN ENUM (MỚI)
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
                            log.info("Đã kích hoạt gói ưu tiên thành công cho Subscription ID: {}", payment.getReferenceId());
                        } catch (Exception e) {
                            log.error("Lỗi khi kích hoạt gói ưu tiên: ", e);
                        }
                        break;

                    case INSPECTION_FEE:
                        // Sau này thêm code xử lý kiểm định tại đây
                        log.info("Thanh toán phí kiểm định thành công cho ID: {}", payment.getReferenceId());
                        break;

                    case ORDER_DEPOSIT:
                        // Thêm code xử lý đặt cọc tại đây
                        log.info("Đặt cọc thành công cho đơn hàng ID: {}", payment.getReferenceId());
                        break;

                    default:
                        log.info("Giao dịch thành công, không cần kích hoạt dịch vụ đặc biệt. Type: {}", payment.getType());
                        break;
                }
            }

            try {
                notificationService.sendPaymentSuccessEmail(payment);
                notificationService.sendRealTimeNotification(payment.getUser().getId(),
                        "Thanh toán thành công! Bạn đã được cộng " + points + " điểm.", "PAYMENT_SUCCESS");
            } catch(Exception ignored) {}

        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setCompletedAt(LocalDateTime.now());
            paymentRepo.save(payment);

            log.warn("VNPay payment failed: orderId={}, responseCode={}", orderId, responseCode);

            try {
                notificationService.sendPaymentFailedEmail(payment);
                notificationService.sendRealTimeNotification(payment.getUser().getId(),
                        "Thanh toán thất bại. Vui lòng thử lại.", "PAYMENT_FAILED");
            } catch(Exception ignored) {}
        }
    }
}