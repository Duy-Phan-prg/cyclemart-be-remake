package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.ChatMessageRequest;
import com.example.cyclemartberemake.dto.response.ChatMessageResponse;
import com.example.cyclemartberemake.dto.response.ChatRealtimeMessageResponse;
import com.example.cyclemartberemake.dto.response.ChatRoomResponse;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatRealtimeController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void send(@Payload ChatMessageRequest request) {
        ChatMessageResponse saved = chatService.sendMessage(getCurrentUserId(), request);
        ChatRoomResponse room = chatService.getRoom(getCurrentUserId(), saved.getRoomId());
        Long receiverId = room.getBuyerId().equals(saved.getSenderId()) ? room.getSellerId() : room.getBuyerId();

        ChatRealtimeMessageResponse response = ChatRealtimeMessageResponse.builder()
                .roomId(saved.getRoomId())
                .messageId(saved.getId())
                .senderId(saved.getSenderId())
                .receiverId(receiverId)
                .senderName(saved.getSenderName())
                .content(saved.getContent())
                .isRead(saved.getIsRead())
                .createdAt(saved.getCreatedAt())
                .build();

        messagingTemplate.convertAndSend("/topic/chats/" + saved.getRoomId(), response);
        messagingTemplate.convertAndSendToUser(String.valueOf(saved.getSenderId()), "/queue/chats", response);
        messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/chats", response);
        messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/notifications/messages", response);
    }

    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Users user) {
            return user.getId();
        }
        return Long.parseLong(principal.toString());
    }
}
