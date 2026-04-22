package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendOtpEmail(String email, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("CycleMart - Mã xác nhận OTP");
        message.setText("Mã OTP của bạn là: " + otpCode + "\n\n" +
                "Mã này sẽ hết hạn trong 10 phút.\n\n" +
                "Nếu bạn không yêu cầu mã này, vui lòng bỏ qua email này.");
        message.setFrom("noreply@cyclemart.com");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email OTP: " + e.getMessage());
        }
    }

    @Override
    public void sendVerificationSuccessEmail(String email, String fullName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("CycleMart - Xác nhận email thành công");
        message.setText("Xin chào " + fullName + ",\n\n" +
                "Email của bạn đã được xác nhận thành công!\n\n" +
                "Bạn có thể đăng nhập vào tài khoản CycleMart ngay bây giờ.\n\n" +
                "Cảm ơn bạn đã sử dụng CycleMart!");
        message.setFrom("noreply@cyclemart.com");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email xác nhận: " + e.getMessage());
        }
    }
}
