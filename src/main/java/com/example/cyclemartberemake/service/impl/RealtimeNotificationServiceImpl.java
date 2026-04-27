package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.service.RealtimeNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RealtimeNotificationServiceImpl implements RealtimeNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyOrderStatusChange(Long buyerId, Long sellerId, Long paymentId, String newStatus) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "ORDER_STATUS_CHANGE");
        payload.put("paymentId", paymentId);
        payload.put("status", newStatus);
        payload.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSendToUser(String.valueOf(buyerId), "/queue/orders", payload);
        if (sellerId != null) {
            messagingTemplate.convertAndSendToUser(String.valueOf(sellerId), "/queue/orders", payload);
        }
    }

    @Override
    public void notifyDisputeStatusChange(Long buyerId, Long sellerId, Long disputeId, String newStatus) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "DISPUTE_STATUS_CHANGE");
        payload.put("disputeId", disputeId);
        payload.put("status", newStatus);
        payload.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSendToUser(String.valueOf(buyerId), "/queue/disputes", payload);
        messagingTemplate.convertAndSendToUser(String.valueOf(sellerId), "/queue/disputes", payload);
    }

    @Override
    public void notifyPointsChange(Long userId, Long pointsDelta, Long newBalance, String reason) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "POINTS_CHANGE");
        payload.put("pointsDelta", pointsDelta);
        payload.put("newBalance", newBalance);
        payload.put("reason", reason);
        payload.put("timestamp", LocalDateTime.now());

        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/points", payload);
    }
}
