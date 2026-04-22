package com.example.cyclemartberemake.service;

public interface OtpService {
    String generateAndSendOtp(String email);
    boolean verifyOtp(String email, String otpCode);
    void cleanExpiredOtps();
}
