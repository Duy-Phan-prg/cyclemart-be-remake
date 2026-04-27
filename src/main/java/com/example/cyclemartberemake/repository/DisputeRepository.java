package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.Dispute;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DisputeRepository extends JpaRepository<Dispute, Long> {
    Page<Dispute> findByBuyerId(Long buyerId, Pageable pageable);
    Page<Dispute> findBySellerId(Long sellerId, Pageable pageable);
    boolean existsByPaymentIdAndBuyerId(Long paymentId, Long buyerId);

    @Query("SELECT COUNT(d) FROM Dispute d WHERE d.payment.bikePost.id = :postId AND d.status NOT IN ('RESOLVED_REFUND_BUYER', 'RESOLVED_RELEASE_SELLER', 'RESOLVED_PARTIAL')")
    long countActiveDisputeByPostId(@Param("postId") Long postId);
}
