package com.example.cyclemartberemake.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidPriorityLevelValidator.class)
@Documented
public @interface ValidPriorityLevel {
    String message() default "Mức ưu tiên không hợp lệ. Chỉ được phép: SILVER, GOLD, PLATINUM";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
