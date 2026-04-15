package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.dto.response.UserLoginResponseDTO;
import com.example.cyclemartberemake.entity.Role;
import com.example.cyclemartberemake.entity.UserStatus;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.UserMapper;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.security.JwtService;
import com.example.cyclemartberemake.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final JwtService jwtService;

    @Override
    public Users register(UserRegisterRequestDTO dto) {


        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Users user = UserMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        user.setRole(Role.BUYER);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    @Override
    public UserLoginResponseDTO login(UserLoginRequestDTO dto) {

        Users user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));


        // Kiểm tra account bị ban
        if (user.getStatus() == UserStatus.BANNED) {
            throw new RuntimeException("Tài khoản đã bị ban");
        }


        // Kiểm tra account bị suspend
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new RuntimeException("Tài khoản đã bị tạm khóa");
        }



        // Login thành công - reset login attempts
        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 🔥 tạo token
        String token = jwtService.generateToken(user.getEmail());

        return new UserLoginResponseDTO(token);
    }
}