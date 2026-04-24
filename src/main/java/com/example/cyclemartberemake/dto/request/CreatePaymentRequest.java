package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePaymentRequest {

    // Đã bỏ @NotNull để có thể dùng chung cho nhiều loại thanh toán
    private Long bikePostId;

    private Long amount;

    @NotBlank(message = "Tên người nhận không được để trống")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    private String type;
    private Long referenceId;
}