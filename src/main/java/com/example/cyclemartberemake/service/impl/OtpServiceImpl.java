package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.entity.OtpVerification;
import com.example.cyclemartberemake.repository.OtpVerificationRepository;
import com.example.cyclemartberemake.service.EmailService;
import com.example.cyclemartberemake.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {

    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;

    @Override
    @Transactional
    public String generateAndSendOtp(String email) {
        // Generate 6-digit OTP
        String otpCode = generateOtpCode();

        // Check if OTP already exists for this email
        Optional<OtpVerification> existingOtp = otpRepository.findByEmail(email);
        
        OtpVerification otp;
        if (existingOtp.isPresent()) {
            // Update existing OTP
            otp = existingOtp.get();
            otp.setOtpCode(otpCode);
            otp.setIsVerified(false);
            otp.setCreatedAt(LocalDateTime.now());
            otp.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        } else {
            // Create new OTP
            otp = OtpVerification.builder()
                    .email(email)
                    .otpCode(otpCode)
                    .isVerified(false)
                    .createdAt(LocalDateTime.now())
                    .expiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES))
                    .build();
        }

        otpRepository.save(otp);

        // Send OTP via email
        emailService.sendOtpEmail(email, otpCode);

        return otpCode;
    }

    @Override
    @Transactional
    public boolean verifyOtp(String email, String otpCode) {
        Optional<OtpVerification> otpOptional = otpRepository.findByEmailAndOtpCode(email, otpCode);

        if (otpOptional.isEmpty()) {
            return false;
        }

        OtpVerification otp = otpOptional.get();

        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(otp.getExpiresAt())) {
            return false;
        }

        // Mark as verified
        otp.setIsVerified(true);
        otp.setVerifiedAt(LocalDateTime.now());
        otpRepository.save(otp);

        return true;
    }

    @Override
    @Transactional
    public void cleanExpiredOtps() {
        // Delete expired OTPs (older than 1 hour)
        otpRepository.deleteAll(otpRepository.findAll().stream()
                .filter(otp -> LocalDateTime.now().isAfter(otp.getExpiresAt().plusHours(1)))
                .toList());
    }

    private String generateOtpCode() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}
