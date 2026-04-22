package com.example.cyclemartberemake.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BikePostResponse {

    private Long id;

    // ================= BASIC =================
    private String title;
    private String description;
    private Double price;

    private String status;

    // ================= LOCATION =================
    private String city;
    private String district;

    // ================= BIKE INFO =================
    private String brand;
    private String model;
    private Integer year;

    private String frameMaterial;
    private String frameSize;
    private String brakeType;
    private String groupset;

    private Integer mileage;

    // ================= CATEGORY =================
    private String categoryName;

    // ================= BUSINESS =================
    private Boolean allowNegotiation;

    // ================= MEDIA =================
    private List<String> images;

    // ================= SELLER =================
    private Long userId;
    private String sellerName;
    private String sellerEmail;

    // ================= PRIORITY =================
    @Schema(description = "Priority package info (if post has active priority)")
    private PriorityPackageResponse activePriority;

    // ================= MODERATION =================
    @Schema(description = "Trạng thái duyệt bài", example = "PENDING / APPROVED / REJECTED")
    private String postStatus;

    @Schema(description = "Hiển thị trạng thái duyệt bài", example = "Chờ duyệt / Đã duyệt / Bị từ chối")
    private String postStatusDisplay;

    @Schema(description = "Lý do từ chối (nếu bị reject)")
    private String rejectionReason;

    // ================= STATS =================
    @Schema(description = "Lượt xem bài đăng", example = "0")
    private Integer viewCount;

    // ================= TIME =================
    private LocalDateTime createdAt;
    private Boolean isVerified;
}