package com.example.cyclemartberemake.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponseDTO {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private String role;
    private String roleDisplay;
    private String status;
    private String statusDisplay;
    private Integer point;  // 🔥 Thêm điểm tích lũy
    private LocalDateTime createdAt;
}