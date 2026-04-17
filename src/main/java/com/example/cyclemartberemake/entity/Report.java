package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title; // Tiêu đề báo cáo

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả chi tiết

    @Enumerated(EnumType.STRING)
    private ReportType type; // Loại báo cáo

    @Enumerated(EnumType.STRING)
    private ReportStatus status; // Trạng thái xử lý

    // Người báo cáo
    @ManyToOne
    @JoinColumn(name = "reporter_id")
    private Users reporter;

    // Người bị báo cáo
    @ManyToOne
    @JoinColumn(name = "reported_user_id")
    private Users reportedUser;

    // Bài đăng bị báo cáo (nếu có)
    @ManyToOne
    @JoinColumn(name = "reported_post_id")
    private BikePost reportedPost;

    // Admin xử lý
    @ManyToOne
    @JoinColumn(name = "handled_by_id")
    private Users handledBy;

    @Column(columnDefinition = "TEXT")
    private String adminNote; // Ghi chú của admin

    private LocalDateTime createdAt;
    private LocalDateTime handledAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = ReportStatus.PENDING;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (this.status == ReportStatus.RESOLVED || this.status == ReportStatus.REJECTED) {
            this.handledAt = LocalDateTime.now();
        }
    }
}