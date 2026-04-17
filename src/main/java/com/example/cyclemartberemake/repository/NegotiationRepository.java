package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.Negotiation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NegotiationRepository extends JpaRepository<Negotiation, Long> {
    List<Negotiation> findByBikePostId(Long bikePostId);
    List<Negotiation> findByBuyerId(Long buyerId);
}