package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.response.BikePostResponse;
import com.example.cyclemartberemake.service.BikePostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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

    @GetMapping("/posts")
    @Operation(summary = "Get all posts for admin (all statuses)")
    public Page<BikePostResponse> getAllPosts(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            
            @Parameter(description = "Sort direction", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        // Validate sort field to prevent errors
        String validSortBy = validateSortField(sortBy);
        
        // Create sort direction
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        
        // Create pageable with validated parameters
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
    
    /**
     * Validate sort field to prevent "No property found" errors
     */
    private String validateSortField(String sortBy) {
        // List of valid BikePost fields for sorting
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
            default -> "createdAt"; // Default fallback
        };
    }
}