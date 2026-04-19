package com.example.cyclemartberemake.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("status", "error");
        response.put("message", "Validation failed");
        response.put("errors", errors);
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException ex) {

        Map<String, Object> response = new HashMap<>();
        String message = ex.getMessage();

        // Check if it's an enum deserialization error
        if (message != null && message.contains("Cannot deserialize value of type") && message.contains("PriorityLevel")) {
            // Extract the valid values from error message
            // Error format: "Cannot deserialize value of type `com.example.cyclemartberemake.entity.PriorityLevel` from String \"PLATINU\": not one of the values accepted for Enum class: [GOLD, PLATINUM, SILVER]"

            String errorMsg = "Mức ưu tiên không hợp lệ. Các giá trị được phép: SILVER, GOLD, PLATINUM";

            response.put("status", "error");
            response.put("message", errorMsg);
            response.put("field", "priorityLevel");
            response.put("acceptedValues", new String[]{"SILVER", "GOLD", "PLATINUM"});

            return ResponseEntity.badRequest().body(response);
        }

        // Generic JSON parse error
        response.put("status", "error");
        response.put("message", "Lỗi định dạng JSON. Vui lòng kiểm tra dữ liệu nhập vào");

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(CategoryValidationException.class)
    public ResponseEntity<Map<String, Object>> handleCategoryValidationException(
            CategoryValidationException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(org.springframework.data.mapping.PropertyReferenceException.class)
    public ResponseEntity<Map<String, Object>> handlePropertyReferenceException(
            org.springframework.data.mapping.PropertyReferenceException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", "Trường sort không hợp lệ. Các trường hợp lệ: id, title, price, createdAt, updatedAt, postStatus, brand, city, year");
        response.put("validSortFields", new String[]{"id", "title", "price", "createdAt", "updatedAt", "postStatus", "brand", "city", "year"});
        
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", ex.getMessage());
        
        return ResponseEntity.badRequest().body(response);
    }
}