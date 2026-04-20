package com.example.cyclemartberemake.dto.response;

import com.example.cyclemartberemake.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private String orderId;
    private Long amount;
    private PaymentStatus status;
    private String statusDisplay;
    private String description;
    private Integer pointsEarned;
    private String message;
    
    // 🔥 Location information
    private String city;
    private String district;
    private String ipAddress;
    
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}