package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.ChatMessageRequest;
import com.example.cyclemartberemake.dto.request.ChatRoomRequest;
import com.example.cyclemartberemake.dto.response.ChatMessageResponse;
import com.example.cyclemartberemake.dto.response.ChatRoomResponse;
import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.ChatMessage;
import com.example.cyclemartberemake.entity.ChatRoom;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.ChatMessageRepository;
import com.example.cyclemartberemake.repository.ChatRoomRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository roomRepository;
    private final ChatMessageRepository messageRepository;
    private final BikePostRepository bikePostRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ChatRoomResponse createOrGetRoom(Long currentUserId, ChatRoomRequest request) {
        BikePost post = bikePostRepository.findById(request.getBikePostId())
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        if (post.getUserId() == null) {
            throw new RuntimeException("Bài đăng chưa có người bán");
        }
        if (currentUserId.equals(post.getUserId())) {
            throw new RuntimeException("Người bán không thể tự tạo phòng chat cho chính bài đăng của mình");
        }

        Long buyerId = currentUserId;
        Long sellerId = post.getUserId();
        Long firstId = Math.min(buyerId, sellerId);
        Long secondId = Math.max(buyerId, sellerId);

        ChatRoom room = roomRepository.findByBikePostIdAndBuyerIdAndSellerId(post.getId(), firstId, secondId)
                .orElseGet(() -> roomRepository.save(ChatRoom.builder()
                        .bikePost(post)
                        .buyerId(firstId)
                        .sellerId(secondId)
                        .build()));

        return toRoomResponse(room);
    }

    @Override
    public ChatRoomResponse getRoom(Long currentUserId, Long roomId) {
        return toRoomResponse(getRoomEntity(currentUserId, roomId));
    }

    @Override
    public Page<ChatMessageResponse> getMessages(Long currentUserId, Long roomId, Pageable pageable) {
        getRoomEntity(currentUserId, roomId);
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId, pageable).map(this::toMessageResponse);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long currentUserId, ChatMessageRequest request) {
        ChatRoom room = getRoomEntity(currentUserId, request.getRoomId());
        Users sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        ChatMessage message = ChatMessage.builder()
                .room(room)
                .senderId(sender.getId())
                .content(request.getContent().trim())
                .build();

        return toMessageResponse(messageRepository.save(message));
    }

    @Override
    public List<ChatRoomResponse> getMyRooms(Long currentUserId) {
        return roomRepository.findAll().stream()
                .filter(room -> room.getBuyerId().equals(currentUserId) || room.getSellerId().equals(currentUserId))
                .sorted(Comparator.comparing(ChatRoom::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(this::toRoomResponse)
                .toList();
    }

    private ChatRoom getRoomEntity(Long currentUserId, Long roomId) {
        ChatRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));
        if (!room.getBuyerId().equals(currentUserId) && !room.getSellerId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền xem phòng chat này");
        }
        return room;
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room) {
        BikePost post = room.getBikePost();
        Users buyer = room.getBuyerId() != null ? userRepository.findById(room.getBuyerId()).orElse(null) : null;
        Users seller = room.getSellerId() != null ? userRepository.findById(room.getSellerId()).orElse(null) : null;

        ChatMessage lastMessage = messageRepository.findByRoomIdOrderByCreatedAtAsc(room.getId(), Pageable.unpaged())
                .stream()
                .reduce((first, second) -> second)
                .orElse(null);

        return ChatRoomResponse.builder()
                .id(room.getId())
                .bikePostId(post != null ? post.getId() : null)
                .bikePostTitle(post != null ? post.getTitle() : null)
                .buyerId(room.getBuyerId())
                .buyerName(buyer != null ? buyer.getFullName() : null)
                .sellerId(room.getSellerId())
                .sellerName(seller != null ? seller.getFullName() : null)
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : room.getUpdatedAt())
                .build();
    }

    private ChatMessageResponse toMessageResponse(ChatMessage message) {
        Users sender = message.getSender();
        return ChatMessageResponse.builder()
                .id(message.getId())
                .roomId(message.getRoom() != null ? message.getRoom().getId() : null)
                .senderId(message.getSenderId())
                .senderName(sender != null ? sender.getFullName() : null)
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}
