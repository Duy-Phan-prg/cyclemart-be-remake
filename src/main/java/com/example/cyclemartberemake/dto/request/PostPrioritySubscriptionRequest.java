package com.example.cyclemartberemake.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PostPrioritySubscriptionRequest {

    @NotNull(message = "ID bài post không được để trống")
    @Min(value = 1, message = "ID bài post không hợp lệ")
    @Schema(description = "ID bài post", example = "1")
    private Long postId;

    @NotNull(message = "ID gói ưu tiên không được để trống")
    @Min(value = 1, message = "ID gói không hợp lệ")
    @Schema(description = "ID gói ưu tiên", example = "1")
    private Long packageId;
}
