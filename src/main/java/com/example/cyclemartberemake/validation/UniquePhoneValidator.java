package com.example.cyclemartberemake.validation;

import com.example.cyclemartberemake.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UniquePhoneValidator implements ConstraintValidator<UniquePhone, String> {

    @Autowired
    private UserRepository userRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Null hoặc empty phone là valid (tuỳ chọn)
        if (value == null || value.trim().isEmpty()) {
            return true;
        }

        // Kiểm tra phone đã tồn tại chưa
        return !userRepository.existsByPhone(value);
    }
}
