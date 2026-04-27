package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WithdrawalResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long amount;
    private String bankName;
    private String accountNumber;
    private String accountHolder;
    private String status;
    private String adminNote;
    private String processedByName;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
}
