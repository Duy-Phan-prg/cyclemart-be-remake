package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequestDTO {

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Tên không được để trống")
    private String fullName;

    private String phone;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password phải >= 6 ký tự")
    private String password;
}