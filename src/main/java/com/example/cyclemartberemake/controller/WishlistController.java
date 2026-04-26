package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.response.WishlistItemResponse;
import com.example.cyclemartberemake.service.WishlistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist Management", description = "APIs for managing post wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping("/{postId}")
    @Operation(summary = "Add a post to current user's wishlist")
    public WishlistItemResponse addToWishlist(@PathVariable Long postId) {
        return wishlistService.addPostToWishlist(postId);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Remove a post from current user's wishlist")
    public Map<String, String> removeFromWishlist(@PathVariable Long postId) {
        wishlistService.removePostFromWishlist(postId);
        return Map.of("message", "Đã xóa bài đăng khỏi wishlist");
    }

    @GetMapping
    @Operation(summary = "Get current user's wishlist")
    public Page<WishlistItemResponse> getMyWishlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return wishlistService.getMyWishlist(pageable);
    }

    @GetMapping("/check/{postId}")
    @Operation(summary = "Check if a post is in current user's wishlist")
    public Map<String, Boolean> checkInWishlist(@PathVariable Long postId) {
        return Map.of("isInWishlist", wishlistService.isPostInMyWishlist(postId));
    }
}
