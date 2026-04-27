package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.SellerRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;


@Repository
public interface SellerRatingRepository extends JpaRepository<SellerRating, Long> {

    Page<SellerRating> findBySellerIdOrSeller_Id(Long sellerId, Long sellerId2, Pageable pageable);

    Page<SellerRating> findBySeller_Id(Long sellerId, Pageable pageable);

    Optional<SellerRating> findByPaymentId(Long paymentId);

    boolean existsByPaymentId(Long paymentId);

    Optional<SellerRating> findTopBySeller_IdAndBuyer_IdOrderByCreatedAtDesc(Long sellerId, Long buyerId);

    Page<SellerRating> findByBuyer_Id(Long buyerId, Pageable pageable);

    long countBySeller_Id(Long sellerId);

    @Query("SELECT AVG(sr.score) FROM SellerRating sr WHERE sr.seller.id = :sellerId")
    Double getAverageScoreBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT sr.payment.id FROM SellerRating sr WHERE sr.payment.id IN :paymentIds")
    Set<Long> findRatedPaymentIds(@Param("paymentIds") Collection<Long> paymentIds);
}
