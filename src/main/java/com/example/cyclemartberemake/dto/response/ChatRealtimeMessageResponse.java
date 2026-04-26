package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatRealtimeMessageResponse {
    private Long roomId;
    private Long messageId;
    private Long senderId;
    private Long receiverId;
    private String senderName;
    private String content;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
