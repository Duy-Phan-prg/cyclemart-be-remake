package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentResponse {
    private String orderId;
    private Long amount;
    private String description;
    private String paymentUrl;  // VNPay payment URL
    private String payUrl;      // Legacy field
    private String qrUrl;       // Legacy field
    private String qrCodeUrl;   // Legacy field
    private String deeplink;    // Legacy field
    private String message;
    private boolean success;
}