package com.example.cyclemartberemake.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminNotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String message;
    private Integer count;
    private String actionUrl;
    private Boolean isRead;
    private LocalDateTime createdAt;
}