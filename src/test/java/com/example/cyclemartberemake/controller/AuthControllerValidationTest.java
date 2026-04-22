package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.UserRegisterRequestDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Auth Controller - Registration Validation Tests")
class AuthControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should return error when email is blank")
    void testEmailBlankValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("");
        dto.setFullName("John Doe");
        dto.setPhone("0123456789");
        dto.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.email").value(containsString("Email không được để trống")));
    }

    @Test
    @DisplayName("Should return error when email is invalid format")
    void testEmailInvalidFormatValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("invalid-email");
        dto.setFullName("John Doe");
        dto.setPhone("0123456789");
        dto.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.email").value(containsString("Email không hợp lệ")));
    }

    @Test
    @DisplayName("Should return error when fullName is blank")
    void testFullNameBlankValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("test@example.com");
        dto.setFullName("");
        dto.setPhone("0123456789");
        dto.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.fullName").value(containsString("Tên không được để trống")));
    }

    @Test
    @DisplayName("Should return error when fullName is too short")
    void testFullNameTooShortValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("test@example.com");
        dto.setFullName("A");
        dto.setPhone("0123456789");
        dto.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.fullName").value(containsString("Tên phải từ 2-100 ký tự")));
    }

    @Test
    @DisplayName("Should return error when phone is blank")
    void testPhoneBlankValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("test@example.com");
        dto.setFullName("John Doe");
        dto.setPhone("");
        dto.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.phone").value(containsString("Số điện thoại không được để trống")));
    }

    @Test
    @DisplayName("Should return error when phone format is invalid")
    void testPhoneInvalidFormatValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("test@example.com");
        dto.setFullName("John Doe");
        dto.setPhone("123456789");
        dto.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.phone").value(containsString("SĐT phải bắt đầu 0 và có đúng 10 số")));
    }

    @Test
    @DisplayName("Should return error when password is blank")
    void testPasswordBlankValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("test@example.com");
        dto.setFullName("John Doe");
        dto.setPhone("0123456789");
        dto.setPassword("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.password").value(containsString("Password không được để trống")));
    }

    @Test
    @DisplayName("Should return error when password lacks uppercase letter")
    void testPasswordNoUppercaseValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("test@example.com");
        dto.setFullName("John Doe");
        dto.setPhone("0123456789");
        dto.setPassword("password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.password").value(containsString("Password phải có ít nhất 1 chữ hoa và 1 ký tự đặc biệt")));
    }

    @Test
    @DisplayName("Should return error when password lacks special character")
    void testPasswordNoSpecialCharValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("test@example.com");
        dto.setFullName("John Doe");
        dto.setPhone("0123456789");
        dto.setPassword("Password123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.password").value(containsString("Password phải có ít nhất 1 chữ hoa và 1 ký tự đặc biệt")));
    }

    @Test
    @DisplayName("Should return multiple errors when multiple fields are invalid")
    void testMultipleFieldsValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("invalid-email");
        dto.setFullName("A");
        dto.setPhone("123");
        dto.setPassword("password");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("Should return error when all 4 fields are blank")
    void testAllFieldsBlankValidation() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("");
        dto.setFullName("");
        dto.setPhone("");
        dto.setPassword("");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.fullName").exists())
                .andExpect(jsonPath("$.errors.phone").exists())
                .andExpect(jsonPath("$.errors.password").exists());
    }

    @Test
    @DisplayName("Should successfully register with valid data")
    void testValidRegistration() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("user@gmail.com");
        dto.setFullName("Nguyen Van A");
        dto.setPhone("0794458895");
        dto.setPassword("StrongPass123!");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("user@gmail.com"))
                .andExpect(jsonPath("$.fullName").value("Nguyen Van A"))
                .andExpect(jsonPath("$.phone").value("0794458895"))
                .andExpect(jsonPath("$.roleDisplay").value("Người mua"))
                .andExpect(jsonPath("$.statusDisplay").value("Hoạt động"))
                .andExpect(jsonPath("$.point").value(0))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("Should successfully register with valid password containing uppercase and special char")
    void testValidRegistrationWithComplexPassword() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("newuser@example.com");
        dto.setFullName("John Doe");
        dto.setPhone("0987654321");
        dto.setPassword("SecurePass@2024");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.fullName").value("John Doe"))
                .andExpect(jsonPath("$.phone").value("0987654321"))
                .andExpect(jsonPath("$.roleDisplay").value("Người mua"))
                .andExpect(jsonPath("$.statusDisplay").value("Hoạt động"));
    }

    @Test
    @DisplayName("Should successfully register with valid full name (2 characters)")
    void testValidRegistrationWithMinFullName() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("ab@example.com");
        dto.setFullName("AB");
        dto.setPhone("0123456789");
        dto.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("AB"))
                .andExpect(jsonPath("$.roleDisplay").value("Người mua"));
    }

    @Test
    @DisplayName("Should successfully register with valid full name (100 characters)")
    void testValidRegistrationWithMaxFullName() throws Exception {
        String maxName = "A".repeat(100);
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("max@example.com");
        dto.setFullName(maxName);
        dto.setPhone("0111111111");
        dto.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value(maxName))
                .andExpect(jsonPath("$.roleDisplay").value("Người mua"));
    }

    @Test
    @DisplayName("Should successfully register with valid phone starting with 0")
    void testValidRegistrationWithValidPhone() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("phone@example.com");
        dto.setFullName("Test User");
        dto.setPhone("0912345678");
        dto.setPassword("ValidPass@123");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.phone").value("0912345678"))
                .andExpect(jsonPath("$.roleDisplay").value("Người mua"));
    }

    @Test
    @DisplayName("Should successfully register with password containing special characters")
    void testValidRegistrationWithSpecialCharPassword() throws Exception {
        UserRegisterRequestDTO dto = new UserRegisterRequestDTO();
        dto.setEmail("special@example.com");
        dto.setFullName("Special User");
        dto.setPhone("0999999999");
        dto.setPassword("Pass@#$%^&*");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.roleDisplay").value("Người mua"))
                .andExpect(jsonPath("$.statusDisplay").value("Hoạt động"));
    }
}
