package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.entity.Role;
import com.example.cyclemartberemake.entity.UserStatus;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.UserMapper;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
    public Users login(UserLoginRequestDTO dto) {
        Users user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        user.setLastLoginAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}