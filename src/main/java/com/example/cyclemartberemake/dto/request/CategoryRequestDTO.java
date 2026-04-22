package com.example.cyclemartberemake.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequestDTO {

    @NotBlank(message = "Tên danh mục không được để trống")
    @Size(min = 2, max = 100, message = "Tên danh mục phải từ 2-100 ký tự")

    @Pattern(regexp = "^[a-zA-Z0-9\\s\\p{L}]+$", message = "Tên danh mục không được chứa ký tự đặc biệt")
    private String name;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String description;

    @Min(value = 1, message = "Parent ID phải là số dương")
    private Integer parentId; // Có thể null cho root category

    @NotNull(message = "Thứ tự hiển thị không được để trống")
    @Min(value = 0, message = "Thứ tự hiển thị phải >= 0")
    @Max(value = 9999, message = "Thứ tự hiển thị phải <= 9999")
    private Integer displayOrder;

    @NotNull(message = "Trạng thái kích hoạt không được để trống")
    private Boolean isActive;
}