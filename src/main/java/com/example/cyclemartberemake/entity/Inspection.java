package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Bài đăng cần kiểm định
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BikePost bikePost;

    // Người yêu cầu (Người bán)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Users seller;

    // Nhân viên kiểm định (Do Admin gán)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspector_id")
    private Users inspector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionStatus status;

    @Column(nullable = false)
    private String address; // Địa chỉ xem xe

    private LocalDateTime scheduledDateTime;// Ngày hẹn xem xe

    private Double inspectionFee; // Phí kiểm định (Tạm set = 0)

    @Column(columnDefinition = "TEXT")
    private String note; // Ghi chú của người bán khi đặt lịch

    @Column(columnDefinition = "TEXT")
    private String resultNote; // Đánh giá của Inspector sau khi xem xe

    @Column(columnDefinition = "TEXT")
    private String checklistData;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (this.status == null) {
            this.status = InspectionStatus.PENDING;
        }
        if (this.inspectionFee == null) {
            this.inspectionFee = 0.0; // Tạm thời để 0 để test
        }
    }
}