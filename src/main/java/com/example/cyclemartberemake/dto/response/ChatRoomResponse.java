package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ChatRoomResponse {
    private Long id;
    private Long bikePostId;
    private String bikePostTitle;
    private Long buyerId;
    private String buyerName;
    private Long sellerId;
    private String sellerName;
    private String lastMessage;
    private LocalDateTime lastMessageAt;
}
