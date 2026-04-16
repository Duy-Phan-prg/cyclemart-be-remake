package com.example.cyclemartberemake.dto.response;

import com.example.cyclemartberemake.entity.PriorityLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PriorityPackageResponse {

    @Schema(description = "ID gói ưu tiên", example = "1")
    private Long id;

    @Schema(description = "Tên gói", example = "Gói Platinum")
    private String name;

    @Schema(description = "Mô tả gói", example = "Ưu tiên đầu tiên trong category")
    private String description;

    @Schema(description = "Giá (VND)", example = "100000")
    private Double price;

    @Schema(description = "Thời hạn (ngày)", example = "7")
    private Integer durationDays;

    @Schema(description = "Mức ưu tiên (SILVER, GOLD, PLATINUM)", example = "PLATINUM")
    private PriorityLevel priorityLevel;


    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;

    @Schema(description = "Ngày tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Ngày cập nhật")
    private LocalDateTime updatedAt;
}

