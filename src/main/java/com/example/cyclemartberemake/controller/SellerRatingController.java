package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.SellerRatingRequest;
import com.example.cyclemartberemake.dto.response.SellerRatingResponse;
import com.example.cyclemartberemake.dto.response.SellerInfoResponse;
import com.example.cyclemartberemake.service.SellerRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/seller-ratings")
@RequiredArgsConstructor
@Tag(name = "Seller Rating Management", description = "APIs for rating sellers")
public class SellerRatingController {

    private final SellerRatingService sellerRatingService;

    /**
     * Tạo hoặc cập nhật đánh giá cho một seller
     */
    @PostMapping
    @Operation(summary = "Create or update seller rating")
    public ResponseEntity<?> createOrUpdateSellerRating(@Valid @RequestBody SellerRatingRequest request) {
        try {
            Long buyerId = getCurrentUserId();
            SellerRatingResponse response = sellerRatingService.createOrUpdateSellerRating(buyerId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Lấy tất cả đánh giá của một seller (seller info + list ratings)
     */
    @GetMapping("/seller/{sellerId}")
    @Operation(summary = "Get seller ratings and info")
    public ResponseEntity<?> getSellerRatings(
            @PathVariable Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            SellerInfoResponse sellerInfo = sellerRatingService.getSellerInfo(sellerId);
            Page<SellerRatingResponse> ratings = sellerRatingService.getSellerRatings(sellerId, pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("seller", sellerInfo);
            response.put("ratings", ratings);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Lấy thông tin seller (điểm trung bình, số lượng đánh giá)
     */
    @GetMapping("/seller/{sellerId}/info")
    @Operation(summary = "Get seller rating info only")
    public ResponseEntity<?> getSellerInfo(@PathVariable Long sellerId) {
        try {
            SellerInfoResponse response = sellerRatingService.getSellerInfo(sellerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Lấy đánh giá của buyer hiện tại cho một seller
     */
    @GetMapping("/seller/{sellerId}/my-rating")
    @Operation(summary = "Get my rating for a specific seller")
    public ResponseEntity<?> getMyRatingForSeller(@PathVariable Long sellerId) {
        try {
            Long buyerId = getCurrentUserId();
            SellerRatingResponse response = sellerRatingService.getSellerRatingByBuyer(sellerId, buyerId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Lấy tất cả đánh giá do buyer hiện tại tạo
     */
    @GetMapping("/my-ratings")
    @Operation(summary = "Get all my seller ratings")
    public ResponseEntity<?> getMySellerRatings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        try {
            Long buyerId = getCurrentUserId();
            Pageable pageable = PageRequest.of(page, size);
            Page<SellerRatingResponse> ratings = sellerRatingService.getMySellerRatings(buyerId, pageable);
            return ResponseEntity.ok(ratings);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Xóa một đánh giá
     */
    @DeleteMapping("/{ratingId}")
    @Operation(summary = "Delete a seller rating")
    public ResponseEntity<?> deleteSellerRating(@PathVariable Long ratingId) {
        try {
            Long buyerId = getCurrentUserId();
            sellerRatingService.deleteSellerRating(ratingId, buyerId);
            return ResponseEntity.ok(createSuccessResponse("Xóa đánh giá thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(createErrorResponse(e.getMessage()));
        }
    }

    /**
     * Lấy user ID từ JWT token
     */
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();

        if (principal instanceof String) {
            return Long.parseLong((String) principal);
        } else {
            String principalStr = principal.toString();
            String idStr = principalStr.substring(principalStr.indexOf("id=") + 3, principalStr.indexOf(","));
            return Long.parseLong(idStr);
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", "error");
        return response;
    }

    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", "success");
        return response;
    }
}
