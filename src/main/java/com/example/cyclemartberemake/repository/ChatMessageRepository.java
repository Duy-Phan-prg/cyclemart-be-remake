package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId, Pageable pageable);
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    long countByRoomIdAndSenderIdNotAndIsReadFalse(Long roomId, Long senderId);

    @Modifying
    @Query("""
            update ChatMessage m
            set m.isRead = true, m.readAt = :readAt
            where m.room.id = :roomId
              and m.senderId <> :currentUserId
              and m.isRead = false
            """)
    int markMessagesAsRead(@Param("roomId") Long roomId,
                           @Param("currentUserId") Long currentUserId,
                           @Param("readAt") LocalDateTime readAt);
}
