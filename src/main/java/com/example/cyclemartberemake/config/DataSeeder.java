package com.example.cyclemartberemake.config;

import com.example.cyclemartberemake.entity.Categories;
import com.example.cyclemartberemake.entity.InspectionCriterion; // 🔥 Thêm Import
import com.example.cyclemartberemake.entity.Role;
import com.example.cyclemartberemake.entity.UserStatus;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.repository.CategoryRepository;
import com.example.cyclemartberemake.repository.InspectionCriterionRepository; // 🔥 Thêm Import
import com.example.cyclemartberemake.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   CategoryRepository categoryRepository,
                                   InspectionCriterionRepository inspectionCriterionRepository, // 🔥 Khai báo Repo mới
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            System.out.println("\n========== INITIALIZING DATABASE ==========\n");

            // ================= ADMIN ACCOUNTS =================
            createAdminAccount(userRepository, passwordEncoder, "admin@cyclemart.com", "Admin CycleMart", "0901000001", "admin123");
            createAdminAccount(userRepository, passwordEncoder, "admin2@cyclemart.com", "Admin 2 CycleMart", "0901000002", "admin123");

            // ================= USER ACCOUNTS =================
            createUserAccount(userRepository, passwordEncoder, "user1@test.com", "Nguyễn Văn A", "0912345001", "31072005Xy09@");
            createUserAccount(userRepository, passwordEncoder, "user2@test.com", "Trần Thị B", "0912345002", "31072005Xy09@");

            // ================= INSPECTOR ACCOUNTS =================
            createInspectorAccount(userRepository, passwordEncoder, "inspector@test.com", "Trần Kiểm Định (Ảo 1)", "0912345678", "inspector123");
            createInspectorAccount(userRepository, passwordEncoder, "inspector2@test.com", "Nguyễn Kiểm Định (Ảo 2)", "0987654321", "inspector123");
            createInspectorAccount(userRepository, passwordEncoder, "inspector3@test.com", "Lê Kiểm Định (Ảo 3)", "0987654322", "inspector123");

            // ================= CATEGORIES =================
            seedCategories(categoryRepository);

            // ================= INSPECTION CRITERIA =================
            seedInspectionCriteria(inspectionCriterionRepository); // 🔥 Gọi hàm khởi tạo hạng mục kiểm định

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
            System.out.println("ADMIN ACCOUNT CREATED: " + email);
        }
    }

    private void createUserAccount(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                   String email, String fullName, String phone, String password) {
        if (!userRepository.existsByEmail(email)) {
            Users user = Users.builder()
                    .fullName(fullName)
                    .email(email)
                    .phone(phone)
                    .passwordHash(passwordEncoder.encode(password))
                    .role(Role.USER)
                    .status(UserStatus.ACTIVE)
                    .point(0)
                    .createdAt(LocalDateTime.now())
                    .build();

            userRepository.save(user);
            System.out.println("USER ACCOUNT CREATED: " + email);
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
            System.out.println("INSPECTOR ACCOUNT CREATED: " + email);
        }
    }

    // ================= HÀM KHỞI TẠO DANH MỤC =================
    private void seedCategories(CategoryRepository categoryRepository) {
        if (categoryRepository.count() == 0) {
            System.out.println("CREATING DEFAULT CATEGORIES...");

            Categories sportBikes = createCategory("Xe đạp thể thao", "Các dòng xe chuyên dụng cho tập luyện, đua và vượt địa hình", null, 1);
            Categories casualBikes = createCategory("Xe đạp phổ thông", "Xe đạp đi lại hàng ngày trong thành phố", null, 2);
            Categories parts = createCategory("Phụ tùng & Phụ kiện", "Linh kiện thay thế và đồ chơi xe đạp", null, 3);
            categoryRepository.saveAll(List.of(sportBikes, casualBikes, parts));

            Categories roadBike = createCategory("Xe đạp đua (Road)", "Tối ưu cho đường nhựa, tốc độ cao", sportBikes, 1);
            Categories mtb = createCategory("Xe đạp địa hình (MTB)", "Giảm xóc tốt, lốp bám đường cho đồi núi", sportBikes, 2);
            Categories touring = createCategory("Xe đạp Touring", "Thiết kế để đi phượt, chở nhiều đồ", sportBikes, 3);
            Categories gravel = createCategory("Xe đạp Gravel", "Kết hợp giữa Road và MTB, đa dụng", sportBikes, 4);

            Categories cityBike = createCategory("Xe đạp thành phố (City)", "Dáng ngồi thoải mái, có giỏ/baga", casualBikes, 1);
            Categories kidsBike = createCategory("Xe đạp trẻ em", "Kích thước nhỏ gọn, an toàn cho bé", casualBikes, 2);
            Categories foldingBike = createCategory("Xe đạp gấp", "Dễ dàng gấp gọn mang lên ô tô, tàu điện", casualBikes, 3);

            Categories groupset = createCategory("Bộ truyền động (Groupset)", "Đề, líp, đùi đĩa Shimano, SRAM...", parts, 1);
            Categories frame = createCategory("Khung & Phuộc", "Khung sườn carbon, nhôm, phuộc nhún", parts, 2);
            Categories wheels = createCategory("Bánh xe & Lốp", "Vành, lốp, ruột xe đạp", parts, 3);

            categoryRepository.saveAll(List.of(
                    roadBike, mtb, touring, gravel, cityBike, kidsBike, foldingBike, groupset, frame, wheels
            ));
            System.out.println("   -> Successfully created 3 Parent Categories and 10 Child Categories.\n");
        }
    }

    private Categories createCategory(String name, String description, Categories parent, int displayOrder) {
        Categories category = new Categories();
        category.setName(name);
        category.setDescription(description);
        category.setParent(parent);
        category.setDisplayOrder(displayOrder);
        category.setIsActive(true);
        return category;
    }

    // ================= HÀM KHỞI TẠO HẠNG MỤC KIỂM ĐỊNH (MỚI) =================
    private void seedInspectionCriteria(InspectionCriterionRepository repository) {
        if (repository.count() == 0) {
            System.out.println("CREATING DEFAULT INSPECTION CRITERIA...");

            List<InspectionCriterion> criteria = List.of(
                    createCriterion("Khung sườn & Sơn", "Kiểm tra nứt gãy, cong vênh, rỉ sét, móp méo, tình trạng lớp sơn zin/sơn lại."),
                    createCriterion("Hệ thống phanh (Thắng)", "Kiểm tra độ mòn má phanh, dây phanh, đĩa/niềng phanh, tay phanh, độ nhạy và an toàn."),
                    createCriterion("Hệ thống truyền động (Groupset)", "Kiểm tra xích, líp, đĩa, củ đề trước/sau, tay đề, dây đề, độ mượt khi sang số."),
                    createCriterion("Bánh xe & Lốp", "Kiểm tra vành (niềng) cong vênh, căm lỏng/gãy, lốp xe (mòn, rách), ruột xe, áp suất lốp."),
                    createCriterion("Hệ thống lái & Yên xe", "Kiểm tra ghi đông, pô tăng, chén cổ (cổ phốt), cốt yên, yên xe có lỏng lẻo hay nứt gãy không."),
                    createCriterion("Phuộc / Giảm xóc (Nếu có)", "Kiểm tra độ nhún, xì dầu, nứt gãy, hoạt động của khóa phuộc (Lockout).")
            );

            repository.saveAll(criteria);
            System.out.println("   -> Successfully created " + criteria.size() + " Inspection Criteria.\n");
        }
    }

    private InspectionCriterion createCriterion(String name, String description) {
        InspectionCriterion criterion = new InspectionCriterion();
        criterion.setName(name);
        criterion.setDescription(description);
        criterion.setIsActive(true);
        return criterion;
    }
}