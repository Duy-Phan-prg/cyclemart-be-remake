package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.ChangePasswordRequest;
import com.example.cyclemartberemake.dto.request.UpdateProfileRequest;
import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.dto.response.UserInfoResponseDTO;
import com.example.cyclemartberemake.dto.response.UserLoginResponseDTO;
import com.example.cyclemartberemake.entity.UserTracking;
import com.example.cyclemartberemake.entity.Users;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {

    Users register(UserRegisterRequestDTO dto);

    UserLoginResponseDTO login(UserLoginRequestDTO dto);
    
    List<UserInfoResponseDTO> getAllUsers();
    
    UserInfoResponseDTO getUserById(int id);

    void updateProfile(int userId, UpdateProfileRequest request);
    void changePassword(int userId, ChangePasswordRequest request);

    Page<UserInfoResponseDTO> getAllUsersForAdmin(Pageable pageable);
    void banUser(Integer userId, String reason);
    void unbanUser(Integer userId);
    Page<UserTracking> getUserTrackingLogs(Integer userId, Pageable pageable);
}