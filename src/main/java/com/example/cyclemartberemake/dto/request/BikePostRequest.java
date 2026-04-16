package com.example.cyclemartberemake.dto.request;

import com.example.cyclemartberemake.entity.BikeStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class BikePostRequest {
    
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10-200 ký tự")
    private String title;
    
    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 20, max = 2000, message = "Mô tả phải từ 20-2000 ký tự")
    private String description;
    
    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "100000", message = "Giá bán phải >= 100,000 VND")
    @DecimalMax(value = "999999999", message = "Giá bán phải <= 999,999,999 VND")
    private Double price;
    
    @NotNull(message = "Tình trạng xe không được để trống")
    private BikeStatus status;

    @NotBlank(message = "Thành phố không được để trống")
    @Size(min = 2, max = 50, message = "Thành phố phải từ 2-50 ký tự")
    private String city;
    
    @NotBlank(message = "Quận/Huyện không được để trống")
    @Size(min = 2, max = 50, message = "Quận/Huyện phải từ 2-50 ký tự")
    private String district;

    @NotBlank(message = "Hãng xe không được để trống")
    @Size(min = 2, max = 50, message = "Hãng xe phải từ 2-50 ký tự")
    private String brand;
    
    @Size(max = 50, message = "Model không được quá 50 ký tự")
    private String model;

    @NotNull(message = "Danh mục không được để trống")
    @Min(value = 1, message = "Danh mục không hợp lệ")
    private Integer categoryId; // Chỉ cho phép category con (leaf category)
}
