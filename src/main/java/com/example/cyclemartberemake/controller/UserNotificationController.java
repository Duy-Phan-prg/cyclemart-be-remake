package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.response.UserNotificationResponse;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final UserNotificationService userNotificationService;

    @GetMapping
    public ResponseEntity<List<UserNotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(userNotificationService.getMyNotifications(getCurrentUserId()));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof Users user) {
            return user.getId();
        }
        return Long.parseLong(principal.toString());
    }
}
