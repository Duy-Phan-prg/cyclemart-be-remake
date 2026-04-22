package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePaymentRequest {

    @NotNull(message = "ID bài đăng không được để trống")
    private Long bikePostId;

    @NotBlank(message = "Tên người nhận không được để trống")
    private String name;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;

    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;
}