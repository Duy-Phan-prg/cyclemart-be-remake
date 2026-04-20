package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.entity.Payment;

public interface PaymentNotificationService {

    void sendPaymentSuccessEmail(Payment payment);

    void sendPaymentFailedEmail(Payment payment);

    void sendRefundNotificationEmail(Payment payment);

    void sendRealTimeNotification(Long userId, String message, String type);

    void sendSMSNotification(String phoneNumber, String message);
}