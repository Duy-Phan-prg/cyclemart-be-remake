package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.response.WishlistItemResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WishlistService {
    WishlistItemResponse addPostToWishlist(Long postId);

    void removePostFromWishlist(Long postId);

    Page<WishlistItemResponse> getMyWishlist(Pageable pageable);

    boolean isPostInMyWishlist(Long postId);
}
