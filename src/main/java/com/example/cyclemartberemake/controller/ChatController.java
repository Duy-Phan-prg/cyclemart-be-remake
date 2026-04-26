package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.ChatMessageRequest;
import com.example.cyclemartberemake.dto.request.ChatRoomRequest;
import com.example.cyclemartberemake.dto.response.ChatMessageResponse;
import com.example.cyclemartberemake.dto.response.ChatRealtimeMessageResponse;
import com.example.cyclemartberemake.dto.response.ChatRoomResponse;
import com.example.cyclemartberemake.dto.response.MarkRoomAsReadResponse;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createOrGetRoom(@Valid @RequestBody ChatRoomRequest request) {
        Long currentUserId = getCurrentUserId();
        ChatRoomResponse room = chatService.createOrGetRoom(currentUserId, request);

        if (Boolean.TRUE.equals(room.getNewlyCreated())) {
            Long receiverId = room.getBuyerId().equals(currentUserId) ? room.getSellerId() : room.getBuyerId();
            String senderName = room.getBuyerId().equals(currentUserId) ? room.getBuyerName() : room.getSellerName();

            Map<String, Object> roomNotification = new HashMap<>();
            roomNotification.put("type", "CHAT_ROOM_CREATED");
            roomNotification.put("roomId", room.getId());
            roomNotification.put("senderId", currentUserId);
            roomNotification.put("receiverId", receiverId);
            roomNotification.put("senderName", senderName);
            roomNotification.put("bikePostTitle", room.getBikePostTitle());
            roomNotification.put("message", (senderName != null ? senderName : "Người dùng") + " đã tạo phòng chat mới");
            roomNotification.put("createdAt", LocalDateTime.now());

            messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/notifications/rooms", roomNotification);
            messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/notifications", roomNotification);
        }

        return ResponseEntity.ok(room);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<ChatRoomResponse>> getMyRooms() {
        return ResponseEntity.ok(chatService.getMyRooms(getCurrentUserId()));
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<ChatRoomResponse> getRoom(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.getRoom(getCurrentUserId(), roomId));
    }

    @GetMapping("/rooms/{roomId}/messages")
    public ResponseEntity<Page<ChatMessageResponse>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(chatService.getMessages(getCurrentUserId(), roomId, pageable));
    }

    @PatchMapping("/rooms/{roomId}/read")
    public ResponseEntity<MarkRoomAsReadResponse> markRoomAsRead(@PathVariable Long roomId) {
        return ResponseEntity.ok(chatService.markRoomAsRead(getCurrentUserId(), roomId));
    }

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
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

        return ResponseEntity.ok(saved);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof Users user) {
            return user.getId();
        }
        return Long.parseLong(principal.toString());
    }
}
