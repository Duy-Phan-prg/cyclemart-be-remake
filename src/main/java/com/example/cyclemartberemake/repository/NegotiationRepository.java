package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {
    List<Negotiation> findByBikePostId(Long bikePostId);
    List<Negotiation> findByBuyerId(Long buyerId);

    @Modifying
    @Query("UPDATE Negotiation n SET n.bikePost = null WHERE n.bikePost.id = :postId")
    void detachBikePost(@Param("postId") Long postId);
}