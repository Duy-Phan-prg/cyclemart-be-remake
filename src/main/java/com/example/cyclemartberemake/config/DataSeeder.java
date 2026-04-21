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

            // ================= INSPECTOR 1 =================
            String inspectorEmail1 = "inspector@test.com";

            if (!userRepository.existsByEmail(inspectorEmail1)) {
                Users inspector1 = Users.builder()
                        .fullName("Trần Kiểm Định (Ảo 1)")
                        .email(inspectorEmail1)
                        .phone("0912345678")
                        .passwordHash(passwordEncoder.encode("123456"))
                        .role(Role.INSPECTOR)
                        .status(UserStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build();

                userRepository.save(inspector1);
                System.out.println("✅ ĐÃ TẠO INSPECTOR 1:");
                System.out.println("   - Email: " + inspectorEmail1);
                System.out.println("   - Pass:  123456");
            }

            // ================= INSPECTOR 2 =================
            String inspectorEmail2 = "inspector2@test.com";

            if (!userRepository.existsByEmail(inspectorEmail2)) {
                Users inspector2 = Users.builder()
                        .fullName("Nguyễn Kiểm Định (Ảo 2)")
                        .email(inspectorEmail2)
                        .phone("0987654321")
                        .passwordHash(passwordEncoder.encode("123456"))
                        .role(Role.INSPECTOR)
                        .status(UserStatus.ACTIVE)
                        .createdAt(LocalDateTime.now())
                        .build();
                userRepository.save(inspector2);
                System.out.println("✅ ĐÃ TẠO INSPECTOR 2:");
                System.out.println("   - Email: " + inspectorEmail2);
                System.out.println("   - Pass:  123456");
            }
                String inspectorEmail3 = "inspector3@test.com";

                if (!userRepository.existsByEmail(inspectorEmail3)) {
                    Users inspector3 = Users.builder()
                            .fullName("Nguyễn Kiểm Định (Ảo 3)")
                            .email(inspectorEmail3)
                            .phone("0987654322")
                            .passwordHash(passwordEncoder.encode("123456"))
                            .role(Role.INSPECTOR)
                            .status(UserStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .build();

                    userRepository.save(inspector3);
                    System.out.println("✅ ĐÃ TẠO INSPECTOR 3:");
                    System.out.println("   - Email: " + inspectorEmail3);
                    System.out.println("   - Pass:  123456");
                }
            }
            ;
        }
    }
