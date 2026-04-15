package com.example.cyclemartberemake.controller;


import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequestDTO dto) {
        return ResponseEntity.ok(userService.login(dto));
    }
}
