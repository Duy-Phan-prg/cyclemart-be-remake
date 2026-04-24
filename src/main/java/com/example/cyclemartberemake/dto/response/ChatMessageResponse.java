package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatMessageResponse {
    private Long id;
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private LocalDateTime createdAt;
}
