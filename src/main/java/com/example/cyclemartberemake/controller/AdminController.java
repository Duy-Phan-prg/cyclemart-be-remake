package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.response.BikePostResponse;
import com.example.cyclemartberemake.dto.response.UserInfoResponseDTO;
import com.example.cyclemartberemake.entity.UserTracking; // Cần import cái này
import com.example.cyclemartberemake.service.BikePostService;
import com.example.cyclemartberemake.service.UserService; // Cần import cái này
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page; // Cần import Page
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@Tag(name = "Admin Management", description = "APIs for admin operations")
public class AdminController {

    private final BikePostService bikePostService;
    private final UserService userService; // Đảm bảo đã khai báo UserService ở đây

    // ================== QUẢN LÝ BÀI ĐĂNG ==================

    @GetMapping("/posts")
    @Operation(summary = "Get all posts for admin (all statuses)")
    public Page<BikePostResponse> getAllPosts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        String validSortBy = validateSortField(sortBy);
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, validSortBy));
        return bikePostService.getAllForAdmin(pageable);
    }

    @PutMapping("/posts/{id}/approve")
    @Operation(summary = "Approve a bike post")
    public void approvePost(@PathVariable Long id) {
        bikePostService.approve(id);
    }

    @PutMapping("/posts/{id}/reject")
    @Operation(summary = "Reject a bike post")
    public void rejectPost(@PathVariable Long id, @RequestParam String reason) {
        bikePostService.reject(id, reason);
    }

    // ================== QUẢN LÝ NGƯỜI DÙNG ==================

    @GetMapping("/users")
    @Operation(summary = "Get all users for admin")
    public Page<UserInfoResponseDTO> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return userService.getAllUsersForAdmin(pageable); // Gọi thông qua userService
    }

    @PutMapping("/users/{id}/ban")
    @Operation(summary = "Ban a user account")
    public void banUser(@PathVariable Integer id, @RequestParam String reason) {
        userService.banUser(id, reason); // Gọi thông qua userService
    }

    @PutMapping("/users/{id}/unban")
    @Operation(summary = "Unban a user account")
    public void unbanUser(@PathVariable Integer id) {
        userService.unbanUser(id); // Gọi thông qua userService
    }

    @GetMapping("/users/{id}/tracking")
    @Operation(summary = "Get tracking logs of a specific user")
    public Page<UserTracking> getUserTracking(
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return userService.getUserTrackingLogs(id, pageable); //
    }

    private String validateSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "id" -> "id";
            case "title" -> "title";
            case "price" -> "price";
            case "createdat", "created_at" -> "createdAt";
            case "updatedat", "updated_at" -> "updatedAt";
            case "poststatus", "post_status" -> "postStatus";
            case "approvedat", "approved_at" -> "approvedAt";
            case "userid", "user_id" -> "userId";
            case "brand" -> "brand";
            case "city" -> "city";
            case "year" -> "year";
            default -> "createdAt";
        };
    }
}