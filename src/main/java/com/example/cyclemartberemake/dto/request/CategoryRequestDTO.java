package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequestDTO {
    
    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2-100 ký tự")
    private String name;
    
    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;
    
    @Size(max = 100, message = "Icon không được quá 100 ký tự")
    private String icon;
    
    @Min(value = 1, message = "Parent ID phải lớn hơn 0")
    private Integer parentId; // Có thể null cho root category
    
    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    @Max(value = 9999, message = "Thứ tự hiển thị phải <= 9999")
    private Integer displayOrder = 0;
    
    private Boolean isActive = true;
}