package com.example.cyclemartberemake.mapper;

import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.entity.Users;

public class UserMapper {
    public static Users toEntity(UserRegisterRequestDTO dto) {
        Users user = new Users();

        user.setEmail(dto.getEmail());
        user.setFullName(dto.getFullName());
        user.setPhone(dto.getPhone());

        return user;
    }
}
