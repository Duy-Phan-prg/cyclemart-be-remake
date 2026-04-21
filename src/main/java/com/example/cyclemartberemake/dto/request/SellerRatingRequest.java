package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SellerRatingRequest {

    @NotNull(message = "Seller ID không được rỗng")
    private Integer sellerId;

    @NotNull(message = "Điểm đánh giá không được rỗng")
    @Min(value = 1, message = "Điểm phải từ 1 đến 5 sao")
    @Max(value = 5, message = "Điểm phải từ 1 đến 5 sao")
    private Integer score;

    private String comment;
}
