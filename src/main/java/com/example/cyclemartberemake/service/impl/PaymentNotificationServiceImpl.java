package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.entity.Payment;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.PaymentNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentNotificationServiceImpl implements PaymentNotificationService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    @Value("${spring.mail.username:noreply@cyclemart.com}")
    private String fromEmail;

    @Value("${app.name:CycleMart}")
    private String appName;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Override
    public void sendPaymentSuccessEmail(Payment payment) {
        try {
            Users user = getUserById(payment.getUserId());
            if (user == null || user.getEmail() == null) {
                log.warn("Cannot send email - user not found or no email: userId={}", payment.getUserId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("✅ Thanh toán thành công - " + appName);
            
            String emailBody = buildSuccessEmailBody(payment, user);
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Payment success email sent to: {} for orderId: {}", user.getEmail(), payment.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to send payment success email for orderId: {}", payment.getOrderId(), e);
        }
    }

    @Override
    public void sendPaymentFailedEmail(Payment payment) {
        try {
            Users user = getUserById(payment.getUserId());
            if (user == null || user.getEmail() == null) {
                log.warn("Cannot send email - user not found or no email: userId={}", payment.getUserId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("❌ Thanh toán thất bại - " + appName);
            
            String emailBody = buildFailedEmailBody(payment, user);
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Payment failed email sent to: {} for orderId: {}", user.getEmail(), payment.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to send payment failed email for orderId: {}", payment.getOrderId(), e);
        }
    }

    @Override
    public void sendRefundNotificationEmail(Payment payment) {
        try {
            Users user = getUserById(payment.getUserId());
            if (user == null || user.getEmail() == null) {
                log.warn("Cannot send email - user not found or no email: userId={}", payment.getUserId());
                return;
            }

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(user.getEmail());
            message.setSubject("💰 Hoàn tiền thành công - " + appName);
            
            String emailBody = buildRefundEmailBody(payment, user);
            message.setText(emailBody);

            mailSender.send(message);
            log.info("Refund notification email sent to: {} for orderId: {}", user.getEmail(), payment.getOrderId());
            
        } catch (Exception e) {
            log.error("Failed to send refund notification email for orderId: {}", payment.getOrderId(), e);
        }
    }

    @Override
    public void sendRealTimeNotification(Long userId, String message, String type) {
        // 🔥 TODO: Implement WebSocket/SSE for real-time notifications
        // For now, just log the notification
        log.info("Real-time notification for userId: {} - Type: {} - Message: {}", userId, type, message);
        
        // 🔥 Future implementation:
        // - WebSocket to send real-time notifications
        // - Push notifications for mobile apps
        // - In-app notification system
    }

    @Override
    public void sendSMSNotification(String phoneNumber, String message) {
        // 🔥 TODO: Implement SMS service integration
        // For now, just log the SMS
        log.info("SMS notification to: {} - Message: {}", phoneNumber, message);
        
        // 🔥 Future implementation:
        // - Twilio integration
        // - Vietnamese SMS providers (Viettel, Vinaphone, etc.)
        // - OTP verification
    }

    // 🔥 Helper methods
    private Users getUserById(Long userId) {
        return userRepository.findById(userId.intValue()).orElse(null);
    }

    private String buildSuccessEmailBody(Payment payment, Users user) {
        return String.format("""
            Xin chào %s,
            
            Thanh toán của bạn đã được xử lý thành công! 🎉
            
            📋 THÔNG TIN GIAO DỊCH:
            • Mã đơn hàng: %s
            • Số tiền: %s
            • Mô tả: %s
            • Thời gian: %s
            • Điểm được cộng: %d điểm
            
            💰 SỐ DƯ ĐIỂM HIỆN TẠI: %d điểm
            
            Cảm ơn bạn đã sử dụng dịch vụ %s!
            
            ---
            Đây là email tự động, vui lòng không trả lời email này.
            Nếu có thắc mắc, liên hệ: support@cyclemart.com
            """,
            user.getFullName() != null ? user.getFullName() : "Khách hàng",
            payment.getOrderId(),
            formatCurrency(payment.getAmount()),
            payment.getDescription(),
            payment.getCompletedAt() != null ? payment.getCompletedAt().format(dateFormat) : "N/A",
            payment.getPointsEarned() != null ? payment.getPointsEarned() : 0,
            user.getPoint(),
            appName
        );
    }

    private String buildFailedEmailBody(Payment payment, Users user) {
        return String.format("""
            Xin chào %s,
            
            Rất tiếc, thanh toán của bạn đã thất bại. ❌
            
            📋 THÔNG TIN GIAO DỊCH:
            • Mã đơn hàng: %s
            • Số tiền: %s
            • Mô tả: %s
            • Thời gian: %s
            • Lý do thất bại: %s
            
            🔄 BẠN CÓ THỂ:
            • Thử lại thanh toán với phương thức khác
            • Kiểm tra thông tin thẻ/tài khoản
            • Liên hệ ngân hàng nếu cần thiết
            
            Nếu cần hỗ trợ, vui lòng liên hệ team support của chúng tôi.
            
            ---
            Đây là email tự động, vui lòng không trả lời email này.
            Nếu có thắc mắc, liên hệ: support@cyclemart.com
            """,
            user.getFullName() != null ? user.getFullName() : "Khách hàng",
            payment.getOrderId(),
            formatCurrency(payment.getAmount()),
            payment.getDescription(),
            payment.getCreatedAt().format(dateFormat),
            payment.getMessage() != null ? payment.getMessage() : "Không xác định"
        );
    }

    private String buildRefundEmailBody(Payment payment, Users user) {
        return String.format("""
            Xin chào %s,
            
            Chúng tôi đã hoàn tiền cho giao dịch của bạn. 💰
            
            📋 THÔNG TIN HOÀN TIỀN:
            • Mã đơn hàng: %s
            • Số tiền hoàn: %s
            • Lý do hoàn tiền: %s
            • Thời gian hoàn tiền: %s
            
            💳 THÔNG TIN NHẬN TIỀN:
            Số tiền sẽ được hoàn về tài khoản/ví điện tử bạn đã sử dụng để thanh toán.
            Thời gian nhận tiền: 1-3 ngày làm việc (tùy thuộc vào ngân hàng).
            
            Nếu có thắc mắc về việc hoàn tiền, vui lòng liên hệ support.
            
            Cảm ơn bạn đã sử dụng dịch vụ %s!
            
            ---
            Đây là email tự động, vui lòng không trả lời email này.
            Nếu có thắc mắc, liên hệ: support@cyclemart.com
            """,
            user.getFullName() != null ? user.getFullName() : "Khách hàng",
            payment.getOrderId(),
            formatCurrency(payment.getAmount()),
            payment.getRefundReason() != null ? payment.getRefundReason() : "Theo yêu cầu",
            payment.getRefundedAt() != null ? payment.getRefundedAt().format(dateFormat) : "N/A",
            appName
        );
    }

    private String formatCurrency(Long amount) {
        return String.format("%,d VND", amount);
    }
}