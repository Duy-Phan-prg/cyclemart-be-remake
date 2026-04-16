package com.example.cyclemartberemake.config; // Đổi package nếu bạn đặt ở thư mục khác

import com.example.cyclemartberemake.entity.Role;
import com.example.cyclemartberemake.entity.UserStatus;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {

        // 1. Tạo tài khoản Admin số 1 (Super Admin)
        if (!userRepository.existsByEmail("admin@cyclemart.com")) {
            Users admin1 = new Users();
            admin1.setEmail("admin@cyclemart.com");
            admin1.setFullName("Super Admin");
            admin1.setPhone("0123456789");
            admin1.setPasswordHash(passwordEncoder.encode("admin123")); // Pass mặc định: admin123
            admin1.setRole(Role.ADMIN);
            admin1.setStatus(UserStatus.ACTIVE);

            userRepository.save(admin1);
            System.out.println(">>> Đã tạo tài khoản Admin 1: admin@cyclemart.com / Mật khẩu: admin123");
        }

        // 2. Tạo tài khoản Admin số 2 (Quản lý)
        if (!userRepository.existsByEmail("manager@cyclemart.com")) {
            Users admin2 = new Users();
            admin2.setEmail("manager@cyclemart.com");
            admin2.setFullName("Quản lý Sàn");
            admin2.setPhone("0987654321");
            admin2.setPasswordHash(passwordEncoder.encode("admin123")); // Pass mặc định: admin123
            admin2.setRole(Role.ADMIN);
            admin2.setStatus(UserStatus.ACTIVE);

            userRepository.save(admin2);
            System.out.println(">>> Đã tạo tài khoản Admin 2: manager@cyclemart.com / Mật khẩu: admin123");
        }
    }
}