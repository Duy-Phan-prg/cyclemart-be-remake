package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.AdminNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

    List<AdminNotification> findAllByOrderByCreatedAtDesc();

    int countByIsReadFalse();

    @Modifying
    @Query("UPDATE AdminNotification n SET n.isRead = true WHERE n.isRead = false")
    void markAllAsRead();

    List<AdminNotification> findByTypeAndIsReadFalse(String type);
}