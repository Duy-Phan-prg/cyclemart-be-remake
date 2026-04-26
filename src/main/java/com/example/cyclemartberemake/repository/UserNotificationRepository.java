package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.UserNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationRepository extends JpaRepository<UserNotification, Long> {
    List<UserNotification> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<UserNotification> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("""
            update UserNotification n
            set n.isRead = true, n.readAt = :readAt
            where n.userId = :userId and n.isRead = false
            """)
    int markAllAsRead(@Param("userId") Long userId, @Param("readAt") LocalDateTime readAt);
}
