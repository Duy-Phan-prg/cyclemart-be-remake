package com.example.cyclemartberemake.mapper;

import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.example.cyclemartberemake.dto.response.UserInfoResponseDTO;
import com.example.cyclemartberemake.entity.Role;
import com.example.cyclemartberemake.entity.UserStatus;
import com.example.cyclemartberemake.entity.Users;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

import static com.cloudinary.provisioning.Account.Role.ADMIN;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "banReason", ignore = true)
    @Mapping(target = "bannedAt", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "sellerRating", ignore = true)
    @Mapping(target = "sellerReviewCount", ignore = true)
    Users toEntity(UserRegisterRequestDTO dto);
    
    @Mapping(source = "role", target = "roleDisplay", qualifiedByName = "roleToDisplay")
    @Mapping(source = "status", target = "statusDisplay", qualifiedByName = "statusToDisplay")
    UserInfoResponseDTO toResponse(Users user);
    
    List<UserInfoResponseDTO> toResponseList(List<Users> users);
    
    @Named("roleToDisplay")
    default String roleToDisplay(Role role) {
        if (role == null) return "";
        if (role == Role.BUYER) return "Người mua";
        if (role == Role.SELLER) return "Người bán";
        if (role == Role.ADMIN) return "Quản trị viên";
        return "";
    }
    
    @Named("statusToDisplay")
    default String statusToDisplay(UserStatus status) {
        if (status == null) return "";
        if (status == UserStatus.ACTIVE) return "Hoạt động";
        if (status == UserStatus.INACTIVE) return "Chưa xác thực";
        if (status == UserStatus.BANNED) return "Đã cấm";
        if (status == UserStatus.SUSPENDED) return "Tạm khóa";
        return "";
    }
}
