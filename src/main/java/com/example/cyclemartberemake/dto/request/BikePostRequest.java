package com.example.cyclemartberemake.dto.request;

import com.example.cyclemartberemake.entity.*;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BikePostRequest {
    
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(min = 10, max = 200, message = "Tiêu đề phải từ 10-200 ký tự")
    @Schema(description = "Tiêu đề bài đăng", example = "Xe đạp Giant Defy Advanced 2 - 2024")
    private String title;
    
    @NotBlank(message = "Mô tả không được để trống")
    @Size(min = 20, max = 2000, message = "Mô tả phải từ 20-2000 ký tự")
    @Schema(description = "Mô tả chi tiết về xe", example = "Xe đạp road bike cao cấp, phù hợp cho đường trường...")
    private String description;
    
    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "100000", message = "Giá bán phải >= 100,000 VND")
    @DecimalMax(value = "999999999", message = "Giá bán phải <= 999,999,999 VND")
    @Schema(description = "Giá bán (VND)", example = "15000000")
    private Double price;
    
    @NotNull(message = "Tình trạng xe không được để trống")
    @Schema(description = "Tình trạng xe", example = "LIKE_NEW")
    private BikeStatus status;

    // Location
    @NotNull(message = "Thành phố không được để trống")
    @Schema(description = "Thành phố", example = "HO_CHI_MINH")
    private City city;
    
    @NotNull(message = "Quận/Huyện không được để trống")
    @Schema(description = "Quận/Huyện", example = "QUAN_1")
    private HCMDistrict district;

    // Basic bike info
    @NotNull(message = "Hãng xe không được để trống")
    @Schema(description = "Thương hiệu xe", example = "GIANT")
    private BikeBrand brand;
    
    @Size(max = 100, message = "Model không được quá 100 ký tự")
    @Schema(description = "Model xe", example = "Defy Advanced 2")
    private String model;
    
    @Min(value = 1990, message = "Năm sản xuất phải >= 1990")
    @Schema(description = "Năm sản xuất", example = "2024")
    private Integer year;

    // Technical specs
    @Schema(description = "Chất liệu khung", example = "CARBON")
    private FrameMaterial frameMaterial;
    
    @Schema(description = "Size khung", example = "M")
    private FrameSize frameSize;
    
    @Schema(description = "Loại phanh", example = "DISC_HYDRAULIC")
    private BrakeType brakeType;
    
    @Schema(description = "Groupset", example = "SHIMANO_105")
    private Groupset groupset;

    @NotNull(message = "Danh mục không được để trống")
    @Min(value = 1, message = "Danh mục không hợp lệ")
    @Schema(description = "ID danh mục xe", example = "1")
    private Integer categoryId;

    private Boolean allowNegotiation;

    private Boolean requestInspection; // Checkbox chọn đki ngay
    @NotBlank(message = "Vui lòng nhập địa chỉ xem xe")
    private String inspectionAddress;
    @NotNull(message = "Vui lòng chọn ngày giờ hẹn xem xe")
    @Future(message = "Ngày giờ hẹn kiểm định không được ở trong quá khứ")
    private LocalDateTime inspectionScheduledDate;
    private String inspectionNote;
}
