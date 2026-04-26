package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.response.UserNotificationResponse;
import com.example.cyclemartberemake.entity.UserNotification;
import com.example.cyclemartberemake.repository.ChatMessageRepository;
import com.example.cyclemartberemake.repository.UserNotificationRepository;
import com.example.cyclemartberemake.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private static final String CHAT_TYPE = "CHAT_MESSAGE";
    private static final String CHAT_ROOM_TYPE = "CHAT_ROOM_CREATED";

    private final UserNotificationRepository userNotificationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public List<UserNotificationResponse> getMyNotifications(Long currentUserId) {
        return userNotificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void createChatMessageNotification(Long receiverId, Long roomId, String senderName, String messageContent) {
        UserNotification notification = UserNotification.builder()
                .userId(receiverId)
                .type(CHAT_TYPE)
                .title("Tin nhắn mới")
                .message((senderName != null ? senderName : "Người dùng") + ": " + messageContent)
                .actionUrl("/chat?roomId=" + roomId)
                .isRead(false)
                .build();
        UserNotification saved = userNotificationRepository.save(notification);
        pushNotificationCreated(receiverId, saved);
    }

    @Override
    @Transactional
    public void createChatRoomNotification(Long receiverId, Long roomId, String creatorName, String bikePostTitle) {
        String postTitle = bikePostTitle != null ? bikePostTitle : "bài đăng";
        UserNotification notification = UserNotification.builder()
                .userId(receiverId)
                .type(CHAT_ROOM_TYPE)
                .title("Phòng chat mới")
                .message((creatorName != null ? creatorName : "Người dùng") + " đã tạo phòng chat mới cho " + postTitle)
                .actionUrl("/chat?roomId=" + roomId)
                .isRead(false)
                .build();
        UserNotification saved = userNotificationRepository.save(notification);
        pushNotificationCreated(receiverId, saved);
    }

    @Override
    @Transactional
    public void markAsRead(Long currentUserId, Long notificationId) {
        UserNotification notification = userNotificationRepository.findByIdAndUserId(notificationId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            UserNotification saved = userNotificationRepository.save(notification);
            pushNotificationRead(currentUserId, saved.getId());
        }
    }

    @Override
    @Transactional
    public int markAllAsRead(Long currentUserId) {
        LocalDateTime now = LocalDateTime.now();
        int markedNotifications = userNotificationRepository.markAllAsRead(currentUserId, now);
        int markedMessages = chatMessageRepository.markAllIncomingMessagesAsRead(currentUserId, now);
        pushNotificationReadAll(currentUserId, markedNotifications, markedMessages);
        return markedNotifications;
    }

    private UserNotificationResponse toResponse(UserNotification notification) {
        return UserNotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .actionUrl(notification.getActionUrl())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }

    private void pushNotificationCreated(Long userId, UserNotification notification) {
        UserNotificationResponse payload = toResponse(notification);
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/notifications",
                payload
        );
        // Frontend notification bell listens on this channel for realtime refresh.
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/notifications/messages",
                payload
        );
    }

    private void pushNotificationRead(Long userId, Long notificationId) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "NOTIFICATION_READ");
        event.put("notificationId", notificationId);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications/events", event);
    }

    private void pushNotificationReadAll(Long userId, int markedNotifications, int markedMessages) {
        Map<String, Object> event = new HashMap<>();
        event.put("event", "NOTIFICATION_READ_ALL");
        event.put("markedNotifications", markedNotifications);
        event.put("markedMessages", markedMessages);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/notifications/events", event);
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/chats/read-sync", event);
    }
}
