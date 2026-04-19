package com.example.cyclemartberemake.dto.request;

import com.example.cyclemartberemake.entity.PriorityLevel;
import com.example.cyclemartberemake.validation.ValidPriorityLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PriorityPackageRequest {

    @Size(min = 5, max = 100, message = "Tên gói phải từ 5-100 ký tự")
    @Schema(description = "Tên gói ưu tiên", example = "Gói Platinum")
    private String name;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    @Schema(description = "Mô tả gói", example = "Ưu tiên đầu tiên trong mục category")
    private String description;

    @DecimalMin(value = "0", inclusive = true, message = "Giá phải lớn hơn 0")
    @Schema(description = "Giá của gói (VND)", example = "100000")
    private Double price;

    @Min(value = 1, message = "Thời hạn phải >= 1 ngày")
    @Max(value = 365, message = "Thời hạn không được vượt quá 365 ngày")
    @Schema(description = "Thời hạn hiệu lực (ngày)", example = "7")
    private Integer durationDays;

    @ValidPriorityLevel(message = "Mức ưu tiên không hợp lệ. Chỉ được phép: SILVER, GOLD, PLATINUM")
    @Schema(description = "Mức ưu tiên (SILVER, GOLD, PLATINUM)", example = "PLATINUM")
    private PriorityLevel priorityLevel;

    @Schema(description = "Kích hoạt gói", example = "true")
    private Boolean isActive;
}
