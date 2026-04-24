package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.ChatMessageRequest;
import com.example.cyclemartberemake.dto.request.ChatRoomRequest;
import com.example.cyclemartberemake.dto.response.ChatMessageResponse;
import com.example.cyclemartberemake.dto.response.ChatRoomResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChatService {
    ChatRoomResponse createOrGetRoom(Long currentUserId, ChatRoomRequest request);
    ChatRoomResponse getRoom(Long currentUserId, Long roomId);
    Page<ChatMessageResponse> getMessages(Long currentUserId, Long roomId, Pageable pageable);
    ChatMessageResponse sendMessage(Long currentUserId, ChatMessageRequest request);
    List<ChatRoomResponse> getMyRooms(Long currentUserId);
}
