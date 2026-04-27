package com.example.cyclemartberemake.service;

public interface RealtimeNotificationService {
    void notifyOrderStatusChange(Long buyerId, Long sellerId, Long paymentId, String newStatus);
    void notifyDisputeStatusChange(Long buyerId, Long sellerId, Long disputeId, String newStatus);
    void notifyPointsChange(Long userId, Long pointsDelta, Long newBalance, String reason);
}
