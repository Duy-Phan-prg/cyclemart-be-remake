package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DisputeResponse {
    private Long id;
    private Long paymentId;
    private String paymentOrderId;
    private Long buyerId;
    private String buyerName;
    private Integer buyerViolationLevel;
    private Long sellerId;
    private String sellerName;
    private Integer sellerViolationLevel;
    private String reason;
    private String evidenceUrls;
    private String status;
    private Boolean verifiedAtPurchase;
    private Boolean buyerFault;
    private String resolutionNote;
    private String resolvedByName;
    private LocalDateTime resolvedAt;
    private LocalDateTime createdAt;
}
