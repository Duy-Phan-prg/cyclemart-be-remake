package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.SellerRating;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerRatingRepository extends JpaRepository<SellerRating, Long> {

    /**
     * Lấy tất cả đánh giá của một seller
     */
    Page<SellerRating> findBySellerId(Integer sellerId, Pageable pageable);

    /**
     * Lấy đánh giá của buyer cho một seller
     */
    Optional<SellerRating> findBySellerIdAndBuyerId(Integer sellerId, Integer buyerId);

    /**
     * Kiểm tra xem buyer đã đánh giá seller chưa
     */
    boolean existsBySellerIdAndBuyerId(Integer sellerId, Integer buyerId);

    /**
     * Lấy tất cả đánh giá do một buyer tạo
     */
    Page<SellerRating> findByBuyerId(Integer buyerId, Pageable pageable);

    /**
     * Đếm số lượng đánh giá của một seller
     */
    long countBySellerId(Integer sellerId);

    /**
     * Tính điểm trung bình của một seller
     */
    @Query("SELECT AVG(sr.score) FROM SellerRating sr WHERE sr.sellerId = :sellerId")
    Double getAverageScoreBySellerId(@Param("sellerId") Integer sellerId);
}
