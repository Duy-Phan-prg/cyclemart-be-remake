package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String fullName;

    @Email(message = "Email không đúng định dạng")
    private String email;

    private String phone;
}