package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 100, message = "Tên phải từ 2-100 ký tự")
    @Pattern(regexp = "^[A-Za-zÀ-ỹà-ỹ\\s]+$", message = "Tên chỉ được chứa chữ cái và khoảng trắng")
    private String fullName;

    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "SĐT phải bắt đầu 0 và có đúng 10 số")
    private String phone;
}