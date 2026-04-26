package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.NegotiationRequestDTO;
import com.example.cyclemartberemake.dto.response.NegotiationResponseDTO;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.service.NegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/negotiations")
@RequiredArgsConstructor
@Tag(name = "Negotiation Management", description = "APIs for price negotiation")
public class NegotiationController {

    private final NegotiationService negotiationService;

    @PostMapping
    @Operation(summary = "Buyer tạo offer")
    public NegotiationResponseDTO create(@RequestBody NegotiationRequestDTO request) {
        return negotiationService.createNegotiation(getCurrentUserId(), request);
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Seller chấp nhận offer, trả về payment URL cho buyer")
    public NegotiationResponseDTO accept(@PathVariable Long id) throws Exception {
        return negotiationService.acceptNegotiation(id, getCurrentUserId());
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "Seller từ chối offer")
    public NegotiationResponseDTO reject(@PathVariable Long id) {
        return negotiationService.rejectNegotiation(id, getCurrentUserId());
    }

    @PutMapping("/{id}/counter")
    @Operation(summary = "Seller counter offer với giá mới")
    public NegotiationResponseDTO counter(@PathVariable Long id, @RequestParam Double counterPrice) {
        return negotiationService.counterNegotiation(id, getCurrentUserId(), counterPrice);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Seller xem tất cả offer trên bài đăng của mình")
    public List<NegotiationResponseDTO> getByPost(@PathVariable Long postId) {
        return negotiationService.getByPostId(postId, getCurrentUserId());
    }

    @GetMapping("/my")
    @Operation(summary = "Buyer xem offer của mình")
    public List<NegotiationResponseDTO> getMy() {
        return negotiationService.getByBuyer(getCurrentUserId());
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return (long) user.getId();
        }
        throw new RuntimeException("Người dùng chưa đăng nhập");
    }
}
