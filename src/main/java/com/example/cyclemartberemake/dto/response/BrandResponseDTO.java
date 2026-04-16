package com.example.cyclemartberemake.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponseDTO {
    @Schema(description = "ID thương hiệu", example = "1")
    private Integer id;

    @Schema(description = "Tên thương hiệu", example = "Giant")
    private String name;

    @Schema(description = "Mô tả thương hiệu")
    private String description;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;

    @Schema(description = "Ngày tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Ngày cập nhật")
    private LocalDateTime updatedAt;
}