package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.UserLoginRequestDTO;
import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.entity.Users;

public interface UserService {

    Users register(UserRegisterRequestDTO dto);

    Users login(UserLoginRequestDTO dto);

}