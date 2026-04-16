package com.example.cyclemartberemake.validation;

import com.example.cyclemartberemake.entity.PriorityLevel;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidPriorityLevelValidator implements ConstraintValidator<ValidPriorityLevel, PriorityLevel> {

    @Override
    public void initialize(ValidPriorityLevel constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(PriorityLevel value, ConstraintValidatorContext context) {
        // Null values are handled by @NotNull
        if (value == null) {
            return true;
        }

        // Check if the value is a valid PriorityLevel enum
        try {
            PriorityLevel.valueOf(value.toString());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
