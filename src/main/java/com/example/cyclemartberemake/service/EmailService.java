package com.example.cyclemartberemake.service;

public interface EmailService {
    void sendOtpEmail(String email, String otpCode);
    void sendVerificationSuccessEmail(String email, String fullName);
}
