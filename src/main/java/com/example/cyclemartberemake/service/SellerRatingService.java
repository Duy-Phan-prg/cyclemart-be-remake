package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.SellerRatingRequest;
import com.example.cyclemartberemake.dto.response.SellerInfoResponse;
import com.example.cyclemartberemake.dto.response.SellerRatingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerRatingService {

    /**
     * Tạo hoặc cập nhật đánh giá cho một seller
     */
    SellerRatingResponse createOrUpdateSellerRating(Integer buyerId, SellerRatingRequest request);

    /**
     * Lấy tất cả đánh giá của một seller
     */
    Page<SellerRatingResponse> getSellerRatings(Integer sellerId, Pageable pageable);

    /**
     * Lấy đánh giá của buyer cho một seller
     */
    SellerRatingResponse getSellerRatingByBuyer(Integer sellerId, Integer buyerId);

    /**
     * Xóa đánh giá
     */
    void deleteSellerRating(Long ratingId, Integer buyerId);

    /**
     * Lấy tất cả đánh giá do buyer tạo
     */
    Page<SellerRatingResponse> getMySellerRatings(Integer buyerId, Pageable pageable);

    /**
     * Lấy thông tin seller (bao gồm điểm trung bình và số lượng đánh giá)
     */
    SellerInfoResponse getSellerInfo(Integer sellerId);
}
