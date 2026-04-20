package com.example.cyclemartberemake.config;

import com.example.cyclemartberemake.entity.Role;
import com.example.cyclemartberemake.entity.UserStatus;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Kiểm tra xem đã có tài khoản inspector ảo chưa, nếu chưa thì tạo mới
            String inspectorEmail = "inspector@test.com";

            if (!userRepository.existsByEmail(inspectorEmail)) {
                Users inspector = Users.builder()
                        .fullName("Trần Kiểm Định (Ảo)")
                        .email(inspectorEmail)
                        .phone("0912345678")
                        .passwordHash(passwordEncoder.encode("123456")) // Mật khẩu mặc định
                        .role(Role.INSPECTOR)
                        .status(UserStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build();

                userRepository.save(inspector);
                System.out.println("✅ ĐÃ TẠO TÀI KHOẢN INSPECTOR ẢO ĐỂ TEST:");
                System.out.println("   - Email: " + inspectorEmail);
                System.out.println("   - Pass:  123456");
            }
        };
    }
}