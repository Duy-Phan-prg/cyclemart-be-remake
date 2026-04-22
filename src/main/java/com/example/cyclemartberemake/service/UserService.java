package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.ChangePasswordRequest;
import com.example.cyclemartberemake.dto.request.UpdateProfileRequest;
import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.dto.response.UserInfoResponseDTO;
import com.example.cyclemartberemake.dto.response.UserLoginResponseDTO;
import com.example.cyclemartberemake.entity.Users;

import java.util.List;

public interface UserService {

    Users register(UserRegisterRequestDTO dto);

    UserLoginResponseDTO login(UserLoginRequestDTO dto);

    List<UserInfoResponseDTO> getAllUsers();

    UserInfoResponseDTO getUserById(Long id);

    void updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    void addPoint(Long userId, int point);

    Users getCurrentUser();

    void activateUserByEmail(String email);

}