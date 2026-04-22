package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentResponse {
    private String orderId;
    private Long amount;
    private String description;
    private String payUrl;      // URL để redirect đến Sepay (deprecated)
    private String qrUrl;       // QR Code URL cho chuyển khoản
    private String qrCodeUrl;   // QR code để scan
    private String deeplink;    // Deep link cho mobile app
    private String message;
    private boolean success;
}