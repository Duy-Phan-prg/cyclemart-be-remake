package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeliveryUpdateRequest {

    @NotBlank(message = "Phương thức giao hàng không được để trống")
    private String deliveryMethod; // HANDOFF hoặc EXTERNAL_SHIPPING

    @NotBlank(message = "Bằng chứng giao hàng không được để trống")
    private String deliveryEvidenceUrls;

    private String deliveryNote;
}
