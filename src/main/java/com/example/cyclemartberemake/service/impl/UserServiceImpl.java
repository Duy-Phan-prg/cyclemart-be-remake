package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.ChangePasswordRequest;
import com.example.cyclemartberemake.dto.request.UpdateProfileRequest;
import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.dto.response.UserInfoResponseDTO;
import com.example.cyclemartberemake.dto.response.UserLoginResponseDTO;
import com.example.cyclemartberemake.entity.Role;
import com.example.cyclemartberemake.entity.UserStatus;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.UserMapper;
import com.example.cyclemartberemake.repository.UserRepository;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
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

        // New users get USER role by default (can both buy and sell)
        user.setRole(Role.USER);
        user.setStatus(UserStatus.INACTIVE);  // Set to INACTIVE until email is verified

        return userRepository.save(user);
    }

    @Override
    public UserLoginResponseDTO login(UserLoginRequestDTO dto) {

        Users user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Sai mật khẩu");
        }

        // Kiểm tra account phải ACTIVE mới được đăng nhập
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new RuntimeException("Tài khoản chưa được xác thực. Vui lòng kiểm tra email để xác thực OTP");
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
    public void updateProfile(Long userId, UpdateProfileRequest request) {
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
    public void changePassword(Long userId, ChangePasswordRequest request) {
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
    @Transactional
    public void resetPasswordByEmail(String email, String newPassword) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void addPoint(Long userId, int point) {

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPoint(user.getPoint() + point);

        userRepository.save(user);
    }

    @Override
    public Users getCurrentUser() {

        Object principal = org.springframework.security.core.context.SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        if (principal instanceof String) {
            Long id = Long.parseLong((String) principal);
            return userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        String principalStr = principal.toString();
        String idStr = principalStr.substring(principalStr.indexOf("id=") + 3, principalStr.indexOf(","));

        Long id = Long.parseLong(idStr);

        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    @Override
    public Page<UserInfoResponseDTO> getAllUsers(Pageable pageable) {
        Page<Users> usersPage = userRepository.findAll(pageable);
        return usersPage.map(userMapper::toResponse);
    }

    @Override
    public UserInfoResponseDTO getUserById(Long id) {
        Users user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void activateUserByEmail(String email) {
        Users user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));

        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    public Users getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User không tồn tại"));
    }

}