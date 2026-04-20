package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
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

    private Long userId;
    private String orderId;
    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String description;
    private String momoTransId;        // Transaction ID từ MoMo
    private String responseCode;       // Response code từ MoMo
    private String message;            // Message từ MoMo
    private String signature;          // Signature để verify
    private Integer pointsEarned;      // Điểm được cộng
    
    // 🔥 Refund information
    private String refundReason;       // Lý do hoàn tiền
    private LocalDateTime refundedAt;  // Thời gian hoàn tiền
    private Long refundedBy;           // Admin ID thực hiện refund

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime completedAt; // Thời gian hoàn thành

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
