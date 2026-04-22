package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.ChangePasswordRequest;
import com.example.cyclemartberemake.dto.request.UpdateProfileRequest;
import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.dto.request.VerifyOtpRequest;
import com.example.cyclemartberemake.dto.response.UserInfoResponseDTO;
import com.example.cyclemartberemake.dto.response.UserLoginResponseDTO;
import com.example.cyclemartberemake.dto.response.OtpResponse;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.UserMapper;
import com.example.cyclemartberemake.service.UserService;
import com.example.cyclemartberemake.service.OtpService;
import com.example.cyclemartberemake.service.EmailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
    private final OtpService otpService;
    private final EmailService emailService;

    @PostMapping("/register")
    @Operation(summary = "Register new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully, OTP sent to email",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OtpResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(mediaType = "application/json", schema = @Schema(example = """
                            {
                              "status": "error",
                              "message": "Validation failed",
                              "errors": {
                                "email": "Email không hợp lệ",
                                "fullName": "Tên phải từ 2-100 ký tự",
                                "phone": "SĐT phải bắt đầu 0 và có đúng 10 số",
                                "password": "Password phải có ít nhất 1 chữ hoa và 1 ký tự đặc biệt"
                              }
                            }
                            """)))
    })
    public ResponseEntity<OtpResponse> register(@Valid @RequestBody UserRegisterRequestDTO dto) {
        Users user = userService.register(dto);
        otpService.generateAndSendOtp(dto.getEmail());
        
        return ResponseEntity.ok(new OtpResponse(
                "Đăng ký thành công! Mã OTP đã được gửi đến email của bạn",
                dto.getEmail(),
                10
        ));
    }

    @PostMapping("/login")
    @Operation(summary = "User login")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserLoginResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid credentials")
    })
    public ResponseEntity<UserLoginResponseDTO> login(
            @RequestBody @Valid UserLoginRequestDTO dto) {

        return ResponseEntity.ok(userService.login(dto));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User info retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
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
    @ApiResponse(responseCode = "200", description = "List of all users",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoResponseDTO.class)))
    public ResponseEntity<List<UserInfoResponseDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @Operation(summary = "Get user by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<UserInfoResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to email")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP sent successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = OtpResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid email")
    })
    public ResponseEntity<OtpResponse> sendOtp(@RequestParam String email) {
        try {
            otpService.generateAndSendOtp(email);
            return ResponseEntity.ok(new OtpResponse(
                    "Mã OTP đã được gửi đến email của bạn",
                    email,
                    10
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new OtpResponse(
                    "Lỗi: " + e.getMessage(),
                    email,
                    null
            ));
        }
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and activate account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP verified successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserInfoResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired OTP")
    })
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            boolean isValid = otpService.verifyOtp(request.getEmail(), request.getOtpCode());
            
            if (!isValid) {
                return ResponseEntity.badRequest().body(new OtpResponse(
                        "Mã OTP không hợp lệ hoặc đã hết hạn",
                        request.getEmail(),
                        null
                ));
            }

            // Activate user
            userService.activateUserByEmail(request.getEmail());
            
            // Send verification success email
            Users user = userService.getUserByEmail(request.getEmail());
            emailService.sendVerificationSuccessEmail(request.getEmail(), user.getFullName());

            UserInfoResponseDTO response = userMapper.toResponse(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new OtpResponse(
                    "Lỗi: " + e.getMessage(),
                    request.getEmail(),
                    null
            ));
        }
    }
}