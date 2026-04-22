package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_verifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String otpCode;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private LocalDateTime verifiedAt;

    private LocalDateTime expiresAt;

    @PrePersist
    public void prePersist() {
        if (this.expiresAt == null && this.createdAt != null) {
            this.expiresAt = this.createdAt.plusMinutes(10);
        }
    }
}
