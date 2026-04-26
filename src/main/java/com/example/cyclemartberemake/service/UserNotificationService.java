package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.response.UserNotificationResponse;

import java.util.List;

public interface UserNotificationService {
    List<UserNotificationResponse> getMyNotifications(Long currentUserId);
    void createChatMessageNotification(Long receiverId, Long roomId, String senderName, String messageContent);
}
