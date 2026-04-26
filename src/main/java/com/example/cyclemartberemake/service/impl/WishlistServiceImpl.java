package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.response.WishlistItemResponse;
import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.BikeImage;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.entity.WishlistItem;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.WishlistRepository;
import com.example.cyclemartberemake.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {

    private final WishlistRepository wishlistRepository;
    private final BikePostRepository bikePostRepository;

    @Override
    @Transactional
    public WishlistItemResponse addPostToWishlist(Long postId) {
        Long currentUserId = getCurrentUserId();

        BikePost post = bikePostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        if (post.getUserId() != null && post.getUserId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không thể thêm bài của chính mình vào wishlist");
        }

        WishlistItem item = wishlistRepository.findByUserIdAndPostId(currentUserId, postId)
                .orElseGet(() -> wishlistRepository.save(WishlistItem.builder()
                        .userId(currentUserId)
                        .postId(postId)
                        .build()));

        return toResponse(item);
    }

    @Override
    @Transactional
    public void removePostFromWishlist(Long postId) {
        Long currentUserId = getCurrentUserId();
        WishlistItem item = wishlistRepository.findByUserIdAndPostId(currentUserId, postId)
                .orElseThrow(() -> new RuntimeException("Bài đăng chưa có trong wishlist"));
        wishlistRepository.delete(item);
    }

    @Override
    public Page<WishlistItemResponse> getMyWishlist(Pageable pageable) {
        Long currentUserId = getCurrentUserId();
        return wishlistRepository.findByUserIdOrderByCreatedAtDesc(currentUserId, pageable)
                .map(this::toResponse);
    }

    @Override
    public boolean isPostInMyWishlist(Long postId) {
        Long currentUserId = getCurrentUserId();
        return wishlistRepository.existsByUserIdAndPostId(currentUserId, postId);
    }

    private WishlistItemResponse toResponse(WishlistItem item) {
        BikePost post = item.getPost();
        Users seller = post != null ? post.getUser() : null;

        List<String> imageUrls = post != null && post.getImages() != null
                ? post.getImages().stream().map(BikeImage::getUrl).toList()
                : Collections.emptyList();

        return WishlistItemResponse.builder()
                .wishlistItemId(item.getId())
                .addedAt(item.getCreatedAt())
                .postId(post != null ? post.getId() : item.getPostId())
                .title(post != null ? post.getTitle() : null)
                .price(post != null ? post.getPrice() : null)
                .postStatus(post != null && post.getPostStatus() != null ? post.getPostStatus().name() : null)
                .city(post != null && post.getCity() != null ? post.getCity().name() : null)
                .brand(post != null && post.getBrand() != null ? post.getBrand().name() : null)
                .images(imageUrls)
                .sellerId(seller != null ? seller.getId() : (post != null ? post.getUserId() : null))
                .sellerName(seller != null ? seller.getFullName() : null)
                .build();
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return user.getId();
        }
        throw new RuntimeException("Người dùng chưa đăng nhập");
    }
}
