package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.ChangePasswordRequest;
import com.example.cyclemartberemake.dto.request.UpdateProfileRequest;
import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.dto.response.UserInfoResponseDTO;
import com.example.cyclemartberemake.dto.response.UserLoginResponseDTO;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.UserMapper;
import com.example.cyclemartberemake.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication & User Management", description = "APIs for authentication and user management")
public class AuthController {

    private final UserService userService;
    private final UserMapper userMapper;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    public ResponseEntity<UserInfoResponseDTO> register(@Valid @RequestBody UserRegisterRequestDTO dto) {
        Users user = userService.register(dto);
        UserInfoResponseDTO response = userMapper.toResponse(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    public ResponseEntity<UserLoginResponseDTO> login(
            @RequestBody @Valid UserLoginRequestDTO dto) {

        return ResponseEntity.ok(userService.login(dto));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    public ResponseEntity<?> getMe() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Users) {
            Users user = (Users) principal;
            UserInfoResponseDTO response = userMapper.toResponse(user);
            return ResponseEntity.ok(response);
        }

        // Nếu không có authentication hoặc là anonymousUser
        return ResponseEntity.status(401).body("Unauthorized - Please login first");
    }

    @PutMapping("/profile")
    @Operation(summary = "Update current user profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        try {
            // Lấy user hiện tại đang đăng nhập từ hệ thống Security
            Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            userService.updateProfile(currentUser.getId(), request);
            return ResponseEntity.ok().body("Cập nhật thông tin cá nhân thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/password") //
    @Operation(summary = "Change current user password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            // Lấy user hiện tại đang đăng nhập
            Users currentUser = (Users) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

            userService.changePassword(currentUser.getId(), request);
            return ResponseEntity.ok().body("Đổi mật khẩu thành công.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<List<UserInfoResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    public ResponseEntity<UserInfoResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }
}