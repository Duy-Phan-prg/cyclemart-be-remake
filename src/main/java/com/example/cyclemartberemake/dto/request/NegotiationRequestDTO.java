package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NegotiationRequestDTO {
    
    @NotNull(message = "ID bài đăng không được để trống")
    private Long bikePostId;
    
    @NotNull(message = "Giá đề xuất không được để trống")
    @DecimalMin(value = "1000", message = "Giá đề xuất phải >= 1,000 VND")
    private Double offeredPrice;
}