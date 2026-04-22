package com.example.cyclemartberemake.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpResponse {
    private String message;
    private String email;
    private Integer expiryMinutes;
}
