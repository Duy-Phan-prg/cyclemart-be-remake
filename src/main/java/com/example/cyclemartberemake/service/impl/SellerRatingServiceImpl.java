package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.SellerRatingRequest;
import com.example.cyclemartberemake.dto.response.SellerRatingResponse;
import com.example.cyclemartberemake.entity.SellerRating;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.SellerRatingMapper;
import com.example.cyclemartberemake.repository.SellerRatingRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.dto.response.SellerInfoResponse;
import com.example.cyclemartberemake.service.SellerRatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SellerRatingServiceImpl implements SellerRatingService {

    private final SellerRatingRepository sellerRatingRepository;
    private final UserRepository userRepository;
    private final SellerRatingMapper sellerRatingMapper;

    @Override
    @Transactional
    public SellerRatingResponse createOrUpdateSellerRating(Long buyerId, SellerRatingRequest request) {
        // Kiểm tra xem seller có tồn tại không
        Users seller = userRepository.findById(request.getSellerId())
                .orElseThrow(() -> new RuntimeException("Người bán không tồn tại"));

        // Kiểm tra xem buyer có tồn tại không
        Users buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Người mua không tồn tại"));

        // Kiểm tra xem buyer có phải là seller không
        if (buyerId.equals(request.getSellerId())) {
            throw new RuntimeException("Bạn không thể đánh giá chính mình");
        }

        // Tìm hoặc tạo rating
        SellerRating rating = sellerRatingRepository.findBySellerIdAndBuyerId(request.getSellerId(), buyerId)
                .orElse(null);

        if (rating == null) {
            // Tạo rating mới
            rating = SellerRating.builder()
                    .seller(seller)
                    .sellerId(request.getSellerId())
                    .buyer(buyer)
                    .buyerId(buyerId)
                    .score(request.getScore())
                    .comment(request.getComment())
                    .build();
        } else {
            // Cập nhật rating cũ
            rating.setScore(request.getScore());
            rating.setComment(request.getComment());
        }

        SellerRating saved = sellerRatingRepository.save(rating);

        // Cập nhật thông tin seller (average score và total reviews)
        updateSellerRatingInfo(seller);

        return sellerRatingMapper.toResponse(saved);
    }

    @Override
    public Page<SellerRatingResponse> getSellerRatings(Long sellerId, Pageable pageable) {
        // Kiểm tra xem seller có tồn tại không
        if (!userRepository.existsById(sellerId)) {
            throw new RuntimeException("Người bán không tồn tại");
        }

        Page<SellerRating> ratings = sellerRatingRepository.findBySellerId(sellerId, pageable);
        List<SellerRatingResponse> responses = sellerRatingMapper.toResponseList(ratings.getContent());
        return new PageImpl<>(responses, pageable, ratings.getTotalElements());
    }

    @Override
    public SellerRatingResponse getSellerRatingByBuyer(Long sellerId, Long buyerId) {
        SellerRating rating = sellerRatingRepository.findBySellerIdAndBuyerId(sellerId, buyerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        return sellerRatingMapper.toResponse(rating);
    }

    @Override
    @Transactional
    public void deleteSellerRating(Long ratingId, Long buyerId) {
        SellerRating rating = sellerRatingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        // Kiểm tra quyền: chỉ buyer hoặc admin mới có thể xóa
        if (!rating.getBuyerId().equals(buyerId)) {
            throw new RuntimeException("Bạn không có quyền xóa đánh giá này");
        }

        sellerRatingRepository.delete(rating);

        // Cập nhật thông tin seller
        Users seller = userRepository.findById(rating.getSellerId())
                .orElseThrow(() -> new RuntimeException("Người bán không tồn tại"));
        updateSellerRatingInfo(seller);
    }

    @Override
    public Page<SellerRatingResponse> getMySellerRatings(Long buyerId, Pageable pageable) {
        Page<SellerRating> ratings = sellerRatingRepository.findByBuyerId(buyerId, pageable);
        List<SellerRatingResponse> responses = sellerRatingMapper.toResponseList(ratings.getContent());
        return new PageImpl<>(responses, pageable, ratings.getTotalElements());
    }

    @Override
    public SellerInfoResponse getSellerInfo(Long sellerId) {
        // Kiểm tra xem seller có tồn tại không
        Users seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Người bán không tồn tại"));

        Double averageScore = sellerRatingRepository.getAverageScoreBySellerId(sellerId);
        long totalRatings = sellerRatingRepository.countBySellerId(sellerId);

        return SellerInfoResponse.builder()
                .sellerId(sellerId)
                .sellerName(seller.getFullName())
                .sellerEmail(seller.getEmail())
                .averageScore(averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : 0.0)
                .totalRatings(totalRatings)
                .build();
    }

    /**
     * Cập nhật thông tin rating của seller dựa trên các đánh giá trong database
     */
    private void updateSellerRatingInfo(Users seller) {
        Double averageScore = sellerRatingRepository.getAverageScoreBySellerId(seller.getId());
        long totalRatings = sellerRatingRepository.countBySellerId(seller.getId());

        seller.setSellerRating(averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : 0.0);
        seller.setSellerReviewCount(totalRatings);

        userRepository.save(seller);
    }
}
