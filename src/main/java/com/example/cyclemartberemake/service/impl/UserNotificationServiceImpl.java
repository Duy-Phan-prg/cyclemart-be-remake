package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.response.UserNotificationResponse;
import com.example.cyclemartberemake.entity.UserNotification;
import com.example.cyclemartberemake.repository.UserNotificationRepository;
import com.example.cyclemartberemake.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserNotificationServiceImpl implements UserNotificationService {

    private static final String CHAT_TYPE = "CHAT_MESSAGE";
    private static final String CHAT_ROOM_TYPE = "CHAT_ROOM_CREATED";

    private final UserNotificationRepository userNotificationRepository;

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
        userNotificationRepository.save(notification);
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
        userNotificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAsRead(Long currentUserId, Long notificationId) {
        UserNotification notification = userNotificationRepository.findByIdAndUserId(notificationId, currentUserId)
                .orElseThrow(() -> new RuntimeException("Thông báo không tồn tại"));

        if (!Boolean.TRUE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
            userNotificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public int markAllAsRead(Long currentUserId) {
        return userNotificationRepository.markAllAsRead(currentUserId, LocalDateTime.now());
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
}
