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
public class PostPrioritySubscriptionResponse {

    @Schema(description = "ID đăng ký", example = "1")
    private Long id;

    @Schema(description = "ID bài post", example = "1")
    private Long postId;

    @Schema(description = "Tiêu đề bài post", example = "Xe đạp Giant...")
    private String postTitle;

    @Schema(description = "Tên gói ưu tiên", example = "Gói Platinum")
    private String packageName;

    @Schema(description = "Mức ưu tiên", example = "PLATINUM")
    private PriorityLevel priorityLevel;

    @Schema(description = "Ngày bắt đầu")
    private LocalDateTime startDate;

    @Schema(description = "Ngày kết thúc")
    private LocalDateTime endDate;

    @Schema(description = "Trạng thái hoạt động", example = "true")
    private Boolean isActive;

    @Schema(description = "Ngày tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Ngày cập nhật")
    private LocalDateTime updatedAt;
}
