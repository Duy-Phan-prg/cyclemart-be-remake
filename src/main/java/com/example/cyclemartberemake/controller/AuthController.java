package com.example.cyclemartberemake.controller;


import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.dto.response.UserInfoResponseDTO;
import com.example.cyclemartberemake.dto.response.UserLoginResponseDTO;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Users> register(@Valid @RequestBody UserRegisterRequestDTO dto) {
        return ResponseEntity.ok(userService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDTO> login(
            @RequestBody @Valid UserLoginRequestDTO dto) {

        return ResponseEntity.ok(userService.login(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMe() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        if (principal instanceof Users) {
            Users user = (Users) principal;
            UserInfoResponseDTO response = new UserInfoResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhone(),
                user.getRole().toString(),
                user.getStatus().toString()
            );
            return ResponseEntity.ok(response);
        }
        
        // Nếu không có authentication hoặc là anonymousUser
        return ResponseEntity.status(401).body("Unauthorized - Please login first");
    }
}
