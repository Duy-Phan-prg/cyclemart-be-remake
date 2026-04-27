package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.ChatMessageRequest;
import com.example.cyclemartberemake.dto.request.ChatRoomRequest;
import com.example.cyclemartberemake.dto.response.ChatMessageResponse;
import com.example.cyclemartberemake.dto.response.ChatRoomResponse;
import com.example.cyclemartberemake.dto.response.MarkRoomAsReadResponse;
import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.ChatMessage;
import com.example.cyclemartberemake.entity.ChatRoom;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.ChatMessageRepository;
import com.example.cyclemartberemake.repository.ChatRoomRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.ChatService;
import com.example.cyclemartberemake.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository roomRepository;
    private final ChatMessageRepository messageRepository;
    private final BikePostRepository bikePostRepository;
    private final UserRepository userRepository;
    private final UserNotificationService userNotificationService;

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

        // Tìm room theo buyer-seller pair (không phụ thuộc bikePost)
        Optional<ChatRoom> existingRoom = roomRepository.findByBuyerIdAndSellerId(firstId, secondId);
        ChatRoom room;
        boolean newlyCreated = false;
        
        if (existingRoom.isPresent()) {
            room = existingRoom.get();
            // Cập nhật currentBikePostId nếu đang xem xe khác
            if (!post.getId().equals(room.getCurrentBikePostId())) {
                room.setCurrentBikePostId(post.getId());
                room.setBikePost(post); // Cập nhật reference
                roomRepository.save(room);
                
                // Gửi tin nhắn context về xe đang xem
                sendBikeContextMessage(room, currentUserId, post);
            }
        } else {
            newlyCreated = true;
            room = roomRepository.save(ChatRoom.builder()
                    .bikePost(post)
                    .currentBikePostId(post.getId())
                    .buyerId(firstId)
                    .sellerId(secondId)
                    .build());
            createAutoGreetingMessage(room, currentUserId);
            sendBikeContextMessage(room, currentUserId, post);
            
            Long receiverId = room.getBuyerId().equals(currentUserId) ? room.getSellerId() : room.getBuyerId();
            String creatorName = userRepository.findById(currentUserId).map(Users::getFullName).orElse("Người dùng");
            userNotificationService.createChatRoomNotification(
                    receiverId,
                    room.getId(),
                    creatorName,
                    post.getTitle()
            );
        }

        return toRoomResponse(room, currentUserId, newlyCreated);
    }

    @Override
    public ChatRoomResponse getRoom(Long currentUserId, Long roomId) {
        return toRoomResponse(getRoomEntity(currentUserId, roomId), currentUserId, false);
    }

    @Override
    @Transactional
    public Page<ChatMessageResponse> getMessages(Long currentUserId, Long roomId, Pageable pageable) {
        getRoomEntity(currentUserId, roomId);
        messageRepository.markMessagesAsRead(roomId, currentUserId, LocalDateTime.now());
        return messageRepository.findByRoomIdOrderByCreatedAtAsc(roomId, pageable).map(this::toMessageResponse);
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(Long currentUserId, ChatMessageRequest request) {
        ChatRoom room = getRoomEntity(currentUserId, request.getRoomId());
        Users sender = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));
        Long receiverId = room.getBuyerId().equals(currentUserId) ? room.getSellerId() : room.getBuyerId();

        ChatMessage message = ChatMessage.builder()
                .room(room)
                .senderId(sender.getId())
                .content(request.getContent().trim())
                .build();

        ChatMessage savedMessage = messageRepository.save(message);
        userNotificationService.createChatMessageNotification(
                receiverId,
                room.getId(),
                sender.getFullName(),
                savedMessage.getContent()
        );
        return toMessageResponse(savedMessage);
    }

    @Override
    public List<ChatRoomResponse> getMyRooms(Long currentUserId) {
        return roomRepository.findAll().stream()
                .filter(room -> room.getBuyerId().equals(currentUserId) || room.getSellerId().equals(currentUserId))
                .sorted(Comparator.comparing(ChatRoom::getUpdatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .map(room -> toRoomResponse(room, currentUserId, false))
                .toList();
    }

    @Override
    @Transactional
    public MarkRoomAsReadResponse markRoomAsRead(Long currentUserId, Long roomId) {
        getRoomEntity(currentUserId, roomId);
        int markedCount = messageRepository.markMessagesAsRead(roomId, currentUserId, LocalDateTime.now());
        return MarkRoomAsReadResponse.builder()
                .roomId(roomId)
                .markedCount(markedCount)
                .build();
    }

    private ChatRoom getRoomEntity(Long currentUserId, Long roomId) {
        ChatRoom room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));
        if (!room.getBuyerId().equals(currentUserId) && !room.getSellerId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền xem phòng chat này");
        }
        return room;
    }

    private ChatRoomResponse toRoomResponse(ChatRoom room, Long currentUserId, boolean newlyCreated) {
        BikePost post = room.getBikePost();
        Users buyer = room.getBuyerId() != null ? userRepository.findById(room.getBuyerId()).orElse(null) : null;
        Users seller = room.getSellerId() != null ? userRepository.findById(room.getSellerId()).orElse(null) : null;

        List<ChatMessage> messages = messageRepository.findByRoomIdOrderByCreatedAtAsc(room.getId());
        ChatMessage lastMessage = messages
                .stream()
                .reduce((first, second) -> second)
                .orElse(null);
        long unreadCount = messageRepository.countByRoomIdAndSenderIdNotAndIsReadFalse(room.getId(), currentUserId);
        boolean lastMessageRead = lastMessage == null
                || lastMessage.getSenderId().equals(currentUserId)
                || Boolean.TRUE.equals(lastMessage.getIsRead());

        return ChatRoomResponse.builder()
                .id(room.getId())
                .newlyCreated(newlyCreated)
                .bikePostId(post != null ? post.getId() : null)
                .bikePostTitle(post != null ? post.getTitle() : null)
                .buyerId(room.getBuyerId())
                .buyerName(buyer != null ? buyer.getFullName() : null)
                .sellerId(room.getSellerId())
                .sellerName(seller != null ? seller.getFullName() : null)
                .lastMessage(lastMessage != null ? lastMessage.getContent() : null)
                .lastMessageRead(lastMessageRead)
                .lastMessageAt(lastMessage != null ? lastMessage.getCreatedAt() : room.getUpdatedAt())
                .unreadCount(unreadCount)
                .hasUnreadMessages(unreadCount > 0)
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
                .isRead(Boolean.TRUE.equals(message.getIsRead()))
                .readAt(message.getReadAt())
                .createdAt(message.getCreatedAt())
                .build();
    }

    private void createAutoGreetingMessage(ChatRoom room, Long senderId) {
        ChatMessage greetingMessage = ChatMessage.builder()
                .room(room)
                .senderId(senderId)
                .content("Mình chào bạn ạ!")
                .build();
        messageRepository.save(greetingMessage);
    }

    private void sendBikeContextMessage(ChatRoom room, Long senderId, BikePost post) {
        String contextMessage = String.format("🏍️ Đang trao đổi về: %s", post.getTitle());
        ChatMessage bikeContextMessage = ChatMessage.builder()
                .room(room)
                .senderId(senderId)
                .content(contextMessage)
                .build();
        messageRepository.save(bikeContextMessage);
    }
}
