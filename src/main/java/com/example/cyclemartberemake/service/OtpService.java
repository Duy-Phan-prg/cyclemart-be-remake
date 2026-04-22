package com.example.cyclemartberemake.service;

public interface OtpService {
    String generateAndSendOtp(String email);
    String resendOtp(String email, String flow);
    boolean verifyOtp(String email, String otpCode);
    void cleanExpiredOtps();
}
