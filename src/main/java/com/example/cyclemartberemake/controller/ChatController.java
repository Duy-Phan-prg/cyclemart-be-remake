package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.ChatMessageRequest;
import com.example.cyclemartberemake.dto.request.ChatRoomRequest;
import com.example.cyclemartberemake.dto.response.ChatMessageResponse;
import com.example.cyclemartberemake.dto.response.ChatRoomResponse;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/rooms")
    public ResponseEntity<ChatRoomResponse> createOrGetRoom(@Valid @RequestBody ChatRoomRequest request) {
        return ResponseEntity.ok(chatService.createOrGetRoom(getCurrentUserId(), request));
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

    @PostMapping("/messages")
    public ResponseEntity<ChatMessageResponse> sendMessage(@Valid @RequestBody ChatMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessage(getCurrentUserId(), request));
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
