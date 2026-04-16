package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.dto.response.UserInfoResponseDTO;
import com.example.cyclemartberemake.dto.response.UserLoginResponseDTO;
import java.util.List;

public interface UserService {

    UserInfoResponseDTO register(UserRegisterRequestDTO dto);

    UserLoginResponseDTO login(UserLoginRequestDTO dto);
    
    List<UserInfoResponseDTO> getAllUsers();
    
    UserInfoResponseDTO getUserById(int id);

}