package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {

    @NotNull(message = "Số tiền không được để trống")
    @Min(value = 10000, message = "Số tiền tối thiểu là 10,000 VND")
    @Max(value = 50000000, message = "Số tiền tối đa là 50,000,000 VND")
    private Long amount;

    private String description;
    
    // 🔥 NEW: Location information
    private String city;        // Thành phố (HCM, Hà Nội, etc.)
    private String district;    // Quận/Huyện
    private String ipAddress;   // IP address for fraud detection (optional)
}