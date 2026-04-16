package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.NegotiationRequestDTO;
import com.example.cyclemartberemake.dto.response.NegotiationResponseDTO;
import com.example.cyclemartberemake.service.NegotiationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/negotiations")
@RequiredArgsConstructor
@Tag(name = "Negotiation Management", description = "APIs for price negotiation")
public class NegotiationController {

    private final NegotiationService negotiationService;

    @PostMapping
    @Operation(summary = "Create new negotiation")
    public NegotiationResponseDTO create(
            @RequestBody NegotiationRequestDTO request
    ) {
        // Lấy user từ JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        
        Long buyerId;
        if (principal instanceof String) {
            // Nếu principal là string (username/email), cần tìm user
            buyerId = Long.parseLong((String) principal);
        } else {
            // Nếu principal là Users object, lấy ID trực tiếp
            String principalStr = principal.toString();
            // Extract ID from "Users{id=1, email='...', ...}"
            String idStr = principalStr.substring(principalStr.indexOf("id=") + 3, principalStr.indexOf(","));
            buyerId = Long.parseLong(idStr);
        }
        
        return negotiationService.createNegotiation(buyerId, request);
    }
}