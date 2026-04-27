package com.example.cyclemartberemake.dto.response;

import com.example.cyclemartberemake.entity.OrderStatus;
import com.example.cyclemartberemake.entity.PaymentStatus;
import com.example.cyclemartberemake.entity.PaymentType;
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

    // Order flow
    private OrderStatus orderStatus;
    private PaymentType type;

    // Buyer info
    private Long userId;
    private String buyerName;

    // Escrow info
    private Long escrowPoints;
    private Boolean verifiedAtPurchase;

    // Delivery info
    private String deliveryMethod;
    private String deliveryEvidenceUrls;
    private LocalDateTime deliveredAt;
    private LocalDateTime autoReleaseAt;
    private LocalDateTime releasedAt;

    // Bike information
    private Long bikePostId;
    private String bikeTitle;
    private Long bikePrice;
    private Long sellerId;
    private String sellerName;
    private String sellerPhone;

    // Location information
    private String city;
    private String district;
    private String ipAddress;

    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    private String address;
    private String adminNote;

    // COD / Seller confirmation
    private LocalDateTime sellerConfirmationDeadline;
    private String sellerRejectionReason;

    // Rating
    private boolean hasRated; // buyer đã đánh giá đơn này chưa
}