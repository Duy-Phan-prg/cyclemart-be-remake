package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DisputeRequest {

    @NotNull(message = "Payment ID không được để trống")
    private Long paymentId;

    @NotBlank(message = "Lý do tranh chấp không được để trống")
    private String reason;

    private String evidenceUrls;
}
