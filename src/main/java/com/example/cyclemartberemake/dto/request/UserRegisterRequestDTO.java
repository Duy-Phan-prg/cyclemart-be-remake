package com.example.cyclemartberemake.dto.request;

import com.example.cyclemartberemake.validation.UniquePhone;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRegisterRequestDTO {

    @Email(message = "Email không hợp lệ")
    @NotBlank(message = "Email không được để trống")
    private String email;

    @NotBlank(message = "Tên không được để trống")
    @Size(min = 2, max = 100, message = "Tên phải từ 2-100 ký tự")
    @Pattern(regexp = "^[A-Za-zÀ-ỹà-ỹ\\s]+$", message = "Tên chỉ được chứa chữ cái và khoảng trắng")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9}$", message = "SĐT phải bắt đầu 0 và có đúng 10 số")
    @UniquePhone
    private String phone;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password phải có ít nhất 8 ký tự")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).+$",
            message = "Password phải có ít nhất 1 chữ hoa và 1 ký tự đặc biệt"
    )
    private String password;
}