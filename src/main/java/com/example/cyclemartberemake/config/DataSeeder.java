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
            System.out.println("\n========== INITIALIZING DATABASE ==========\n");

            // ================= ADMIN ACCOUNTS =================
            createAdminAccount(userRepository, passwordEncoder, "admin@cyclemart.com", "Admin CycleMart", "0901000001", "admin123");
            createAdminAccount(userRepository, passwordEncoder, "admin2@cyclemart.com", "Admin 2 CycleMart", "0901000002", "admin123");

            // ================= INSPECTOR ACCOUNTS =================
            createInspectorAccount(userRepository, passwordEncoder, "inspector@test.com", "Trần Kiểm Định (Ảo 1)", "0912345678", "inspector123");
            createInspectorAccount(userRepository, passwordEncoder, "inspector2@test.com", "Nguyễn Kiểm Định (Ảo 2)", "0987654321", "inspector123");
            createInspectorAccount(userRepository, passwordEncoder, "inspector3@test.com", "Lê Kiểm Định (Ảo 3)", "0987654322", "inspector123");

            System.out.println("\n========== DATABASE INITIALIZATION COMPLETE ==========\n");
        };
    }

    private void createAdminAccount(UserRepository userRepository, PasswordEncoder passwordEncoder, 
                                    String email, String fullName, String phone, String password) {
        if (!userRepository.existsByEmail(email)) {
            Users admin = Users.builder()
                    .fullName(fullName)
                    .email(email)
                    .phone(phone)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .point(0)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(admin);
            System.out.println("ADMIN ACCOUNT CREATED:");
            System.out.println("   Email: " + email);
            System.out.println("   Name:  " + fullName);
            System.out.println("   Phone: " + phone);
            System.out.println("   Pass:  " + password);
            System.out.println();
        }
    }

    private void createInspectorAccount(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                        String email, String fullName, String phone, String password) {
        if (!userRepository.existsByEmail(email)) {
            Users inspector = Users.builder()
                    .fullName(fullName)
                    .email(email)
                    .phone(phone)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(Role.INSPECTOR)
                    .status(UserStatus.ACTIVE)
                    .point(0)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(inspector);
            System.out.println("INSPECTOR ACCOUNT CREATED:");
            System.out.println("   Email: " + email);
            System.out.println("   Name:  " + fullName);
            System.out.println("   Phone: " + phone);
            System.out.println("   Pass:  " + password);
            System.out.println();
        }
    }
}
