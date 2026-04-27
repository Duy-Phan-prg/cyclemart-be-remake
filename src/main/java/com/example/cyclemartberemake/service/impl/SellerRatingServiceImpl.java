package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.SellerRatingRequest;
import com.example.cyclemartberemake.dto.response.SellerRatingResponse;
import com.example.cyclemartberemake.entity.OrderStatus;
import com.example.cyclemartberemake.entity.Payment;
import com.example.cyclemartberemake.entity.PaymentType;
import com.example.cyclemartberemake.entity.SellerRating;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.SellerRatingMapper;
import com.example.cyclemartberemake.repository.PaymentRepository;
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
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final SellerRatingMapper sellerRatingMapper;

    @Override
    @Transactional
    public SellerRatingResponse createSellerRating(Long buyerId, SellerRatingRequest request) {
        Payment payment = paymentRepository.findById(request.getPaymentId())
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (!payment.getUser().getId().equals(buyerId)) {
            throw new RuntimeException("Bạn không phải người mua của đơn hàng này");
        }

        // Chỉ cho phép đánh giá từ giao dịch mua xe (online hoặc COD)
        if (payment.getType() != PaymentType.ORDER_PAYMENT && payment.getType() != PaymentType.DIRECT_PAYMENT) {
            throw new RuntimeException("Chỉ có thể đánh giá từ đơn mua xe");
        }

        // Chỉ cho phép đánh giá sau khi đã nhận hàng
        if (payment.getOrderStatus() != OrderStatus.DELIVERED && payment.getOrderStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Chỉ được đánh giá sau khi xác nhận đã nhận hàng");
        }

        // Đây là đánh giá SELLER (dịch vụ), không phải sản phẩm — không cần bikePost
        if (payment.getSeller() == null || !payment.getSeller().getId().equals(request.getSellerId())) {
            throw new RuntimeException("Người bán không khớp với đơn hàng");
        }

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

        // Mỗi đơn chỉ được đánh giá 1 lần, không cho sửa sau khi đã đánh giá
        if (sellerRatingRepository.existsByPaymentId(request.getPaymentId())) {
            throw new RuntimeException("Đơn hàng này đã được đánh giá, không thể chỉnh sửa");
        }

        SellerRating rating = SellerRating.builder()
                .seller(seller)
                .buyer(buyer)
                .bikePost(payment.getBikePost()) // nullable — chỉ lưu làm context lịch sử
                .payment(payment)
                .score(request.getScore())
                .comment(request.getComment())
                .build();

        SellerRating saved = sellerRatingRepository.save(rating);

        // Cập nhật thông tin seller (average score và total reviews)
        updateSellerRatingInfo(seller);

        // COD không có escrow nên không có releaseEscrow để set COMPLETED → set luôn sau khi rating
        if (payment.getType() == PaymentType.DIRECT_PAYMENT) {
            payment.setOrderStatus(OrderStatus.COMPLETED);
            payment.setCompletedAt(java.time.LocalDateTime.now());
            paymentRepository.save(payment);
        }

        return sellerRatingMapper.toResponse(saved);
    }

    @Override
    public Page<SellerRatingResponse> getSellerRatings(Long sellerId, Pageable pageable) {
        // Kiểm tra xem seller có tồn tại không
        if (!userRepository.existsById(sellerId)) {
            throw new RuntimeException("Người bán không tồn tại");
        }

        Page<SellerRating> ratings = sellerRatingRepository.findBySeller_Id(sellerId, pageable);
        List<SellerRatingResponse> responses = sellerRatingMapper.toResponseList(ratings.getContent());
        return new PageImpl<>(responses, pageable, ratings.getTotalElements());
    }

    @Override
    public SellerRatingResponse getSellerRatingByBuyer(Long sellerId, Long buyerId) {
        SellerRating rating = sellerRatingRepository.findTopBySeller_IdAndBuyer_IdOrderByCreatedAtDesc(sellerId, buyerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));
        return sellerRatingMapper.toResponse(rating);
    }

    @Override
    @Transactional
    public void deleteSellerRating(Long ratingId, Long buyerId) {
        SellerRating rating = sellerRatingRepository.findById(ratingId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đánh giá"));

        if (!rating.getBuyer().getId().equals(buyerId)) {
            throw new RuntimeException("Bạn không có quyền xóa đánh giá này");
        }

        sellerRatingRepository.delete(rating);

        Users seller = rating.getSeller();
        updateSellerRatingInfo(seller);
    }

    @Override
    public Page<SellerRatingResponse> getMySellerRatings(Long buyerId, Pageable pageable) {
        Page<SellerRating> ratings = sellerRatingRepository.findByBuyer_Id(buyerId, pageable);
        List<SellerRatingResponse> responses = sellerRatingMapper.toResponseList(ratings.getContent());
        return new PageImpl<>(responses, pageable, ratings.getTotalElements());
    }

    @Override
    public SellerInfoResponse getSellerInfo(Long sellerId) {
        Users seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new RuntimeException("Người bán không tồn tại"));

        Double averageScore = sellerRatingRepository.getAverageScoreBySellerId(sellerId);
        long totalRatings = sellerRatingRepository.countBySeller_Id(sellerId);

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
        long totalRatings = sellerRatingRepository.countBySeller_Id(seller.getId());

        seller.setSellerRating(averageScore != null ? Math.round(averageScore * 100.0) / 100.0 : 0.0);
        seller.setSellerReviewCount(totalRatings);

        userRepository.save(seller);
    }
}
