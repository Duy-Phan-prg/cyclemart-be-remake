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

    // Helper method: Kiểm tra và mở khóa account nếu đã qua 10 phút
    private Users checkAndUnlockIfExpired(Users user) {
        if (user.getStatus() == UserStatus.LOCKED && user.getBannedAt() != null) {
            long minutesElapsed = ChronoUnit.MINUTES.between(user.getBannedAt(), LocalDateTime.now());
            if (minutesElapsed >= 10) {
                user.setStatus(UserStatus.ACTIVE);
                user.setLoginAttempts(0);
                user.setBannedAt(null);
                userRepository.save(user);
            }
        }
        return user;
    }

    @Override
    public Users register(UserRegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        if (dto.getPhone() != null && userRepository.existsByPhone(dto.getPhone())) {
            throw new RuntimeException("Số điện thoại đã tồn tại");
        }

        Users user = UserMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        user.setRole(Role.BUYER);
        user.setStatus(UserStatus.ACTIVE);
        user.setLoginAttempts(0);

        return userRepository.save(user);
    }

    @Override
    public UserLoginResponseDTO login(UserLoginRequestDTO dto) {

        Users user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        // Kiểm tra và mở khóa account nếu đã qua 10 phút
        user = checkAndUnlockIfExpired(user);

        // Kiểm tra account bị ban
        if (user.getStatus() == UserStatus.BANNED) {
            throw new RuntimeException("Tài khoản đã bị ban");
        }

        // Kiểm tra account bị khóa
        if (user.getStatus() == UserStatus.LOCKED) {
            LocalDateTime lockedTime = user.getBannedAt();
            long minutesElapsed = ChronoUnit.MINUTES.between(lockedTime, LocalDateTime.now());
            long minutesRemaining = 10 - minutesElapsed;
            throw new RuntimeException("Tài khoản đã bị khóa. Vui lòng thử lại sau " + minutesRemaining + " phút");
        }

        // Kiểm tra account bị suspend
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new RuntimeException("Tài khoản đã bị tạm khóa");
        }

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            // Sai password - tăng counter
            user.setLoginAttempts(user.getLoginAttempts() + 1);

            // Nếu sai >= 5 lần → lock account
            if (user.getLoginAttempts() >= 5) {
                user.setStatus(UserStatus.LOCKED);
                user.setBannedAt(LocalDateTime.now());
                userRepository.save(user);
                throw new RuntimeException("Tài khoản đã bị khóa do đăng nhập sai mật khẩu nhiều lần. Vui lòng thử lại sau 10 phút");
            }

            userRepository.save(user);
            throw new RuntimeException("Sai mật khẩu. Lần thử còn lại: " + (5 - user.getLoginAttempts()));
        }

        // Login thành công - reset login attempts
        user.setLoginAttempts(0);
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        // 🔥 tạo token
        String token = jwtService.generateToken(user.getEmail());

        return new UserLoginResponseDTO(token);
    }
}