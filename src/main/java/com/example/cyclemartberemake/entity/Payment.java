package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bike_post_id")
    private BikePost bikePost;

    @NotBlank(message = "Order ID is required")
    private String orderId;
    
    @NotNull(message = "Amount is required")
    @Min(value = 10000, message = "Minimum amount is 10,000 VND")
    @Max(value = 50000000, message = "Maximum amount is 50,000,000 VND")
    private Long amount;

    @NotNull(message = "Payment status is required")
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @NotBlank(message = "Description is required")
    private String description;
    
    private String momoTransId;
    private String responseCode;
    private String message;
    private String signature;
    private Integer pointsEarned;
    
    private String refundReason;
    private LocalDateTime refundedAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "refunded_by")
    private Users refundedBy;
    
    private String city;
    private String district;
    private String ipAddress;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
    // Trong Payment Entity
    @Enumerated(EnumType.STRING)
    private PaymentType type;

    private Long referenceId; // Lưu ID của gói đăng ký hoặc ID của yêu cầu kiểm định

    @Enumerated(EnumType.STRING)
    @Column(name = "order_status")
    private OrderStatus orderStatus;

    // --- Escrow / Point fields ---
    private Long convertedPoints;  // Số điểm quy đổi từ amount VNPay (1 VND = 1 point)
    private Long escrowPoints;     // Điểm đang giữ escrow cho giao dịch này

    private Boolean verifiedAtPurchase; // Snapshot listing.isVerified tại thời điểm thanh toán

    // --- Delivery fields ---
    private String deliveryMethod;          // HANDOFF hoặc EXTERNAL_SHIPPING
    @Column(columnDefinition = "TEXT")
    private String deliveryEvidenceUrls;    // Bằng chứng giao hàng

    // --- Timeline fields ---
    private LocalDateTime deliveredAt;     // Khi buyer confirm received
    private LocalDateTime releasedAt;      // Khi escrow được xử lý (release hoặc refund)
    private LocalDateTime autoReleaseAt;   // Deadline tự động release (7 ngày sau deliveredAt)
}
