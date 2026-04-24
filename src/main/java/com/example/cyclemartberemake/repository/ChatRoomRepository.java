package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByBikePostIdAndBuyerIdAndSellerId(Long bikePostId, Long buyerId, Long sellerId);
}
