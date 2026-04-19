package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.ChangePasswordRequest;
import com.example.cyclemartberemake.dto.request.UpdateProfileRequest;
import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.dto.response.UserInfoResponseDTO;
import com.example.cyclemartberemake.dto.response.UserLoginResponseDTO;
import com.example.cyclemartberemake.entity.Role;
import com.example.cyclemartberemake.entity.UserStatus;
import com.example.cyclemartberemake.entity.UserTracking;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.UserMapper;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.repository.UserTrackingRepository;
import com.example.cyclemartberemake.security.JwtService;
import com.example.cyclemartberemake.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserTrackingRepository userTrackingRepository; // Đã bổ sung repository này
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public Users register(UserRegisterRequestDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }

        Users user = userMapper.toEntity(dto);
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        user.setRole(Role.BUYER);
        user.setStatus(UserStatus.ACTIVE);

        return userRepository.save(user);
    }

    @Override
    public UserLoginResponseDTO login(UserLoginRequestDTO dto) {

        Users user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        // Kiểm tra account bị ban
        if (user.getStatus() == UserStatus.BANNED) {
            throw new RuntimeException("Tài khoản đã bị ban");
        }

        // Kiểm tra account bị suspend
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new RuntimeException("Tài khoản đã bị tạm khóa");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtService.generateToken(user);

        return new UserLoginResponseDTO(token);
    }

    @Override
    @Transactional
    public void updateProfile(int userId, UpdateProfileRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // Chỉ cập nhật FullName nếu có gửi lên và không phải chuỗi rỗng
        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            user.setFullName(request.getFullName());
        }

        // Chỉ cập nhật Email nếu có gửi lên và không phải chuỗi rỗng
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            // Kiểm tra xem email mới có bị trùng với người khác không
            if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email này đã được sử dụng bởi một tài khoản khác.");
            }
            user.setEmail(request.getEmail());
        }

        // Chỉ cập nhật Phone nếu có gửi lên và không phải chuỗi rỗng
        if (request.getPhone() != null && !request.getPhone().trim().isEmpty()) {
            user.setPhone(request.getPhone());
        }

        // Lưu thay đổi vào DB
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(int userId, ChangePasswordRequest request) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng với ID: " + userId));

        // 1. Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp nhau không
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new RuntimeException("Xác nhận mật khẩu mới không khớp.");
        }

        // 2. Kiểm tra mật khẩu cũ có khớp với Hash trong DB không
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu cũ không chính xác.");
        }

        // 3. Mã hóa mật khẩu mới và cập nhật
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);
    }

    @Override
    public List<UserInfoResponseDTO> getAllUsers() {
        List<Users> users = userRepository.findAll();
        return userMapper.toResponseList(users);
    }

    @Override
    public UserInfoResponseDTO getUserById(int id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return userMapper.toResponse(user);
    }

    @Override
    public Page<UserInfoResponseDTO> getAllUsersForAdmin(Pageable pageable) {
        Page<Users> users = userRepository.findAll(pageable);
        // Map sang DTO
        return users.map(userMapper::toResponse);
    }

    @Override
    @Transactional
    public void banUser(Integer userId, String reason) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (user.getRole() == Role.ADMIN) {
            throw new RuntimeException("Không thể khóa tài khoản Admin");
        }

        user.setStatus(UserStatus.BANNED);
        user.setBanReason(reason);
        user.setBannedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unbanUser(Integer userId) {
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        user.setStatus(UserStatus.ACTIVE); // Mở khóa trở lại trạng thái ACTIVE
        user.setBanReason(null);
        user.setBannedAt(null);

        userRepository.save(user);
    }

    @Override
    public Page<UserTracking> getUserTrackingLogs(Integer userId, Pageable pageable) {
        // Lấy lịch sử theo userId từ repository
        return userTrackingRepository.findByUserId(userId, pageable);
    }
}