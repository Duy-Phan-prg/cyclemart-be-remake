package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
import com.example.cyclemartberemake.entity.*;
import com.example.cyclemartberemake.mapper.BikePostMapper;
import com.example.cyclemartberemake.service.BikePostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
@Tag(name = "Bike Post Management", description = "APIs for bike post management")
public class BikePostController {

    private final BikePostService service;
    private final BikePostMapper mapper;

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Create new bike post")
    public BikePostResponse create(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("status") BikeStatus status,

            @RequestParam("city") City city,
            @RequestParam("district") HCMDistrict district,

            @RequestParam("brand") BikeBrand brand,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "year", required = false) Integer year,

            @RequestParam(value = "frameMaterial", required = false) FrameMaterial frameMaterial,
            @RequestParam(value = "frameSize", required = false) FrameSize frameSize,
            @RequestParam(value = "brakeType", required = false) BrakeType brakeType,
            @RequestParam(value = "groupset", required = false) Groupset groupset,
            @RequestParam(value = "mileage", required = false) Integer mileage,

            @RequestParam("categoryId") Integer categoryId,

            @RequestParam(value = "allowNegotiation", required = false, defaultValue = "false") Boolean allowNegotiation,

            // 🔥 THÊM CÁC THAM SỐ KIỂM ĐỊNH TỪ FRONTEND GỬI LÊN
            @RequestParam(value = "requestInspection", required = false, defaultValue = "false") Boolean requestInspection,
            @RequestParam(value = "inspectionAddress", required = false) String inspectionAddress,
            @RequestParam(value = "inspectionScheduledDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inspectionScheduledDate,
            @RequestParam(value = "inspectionNote", required = false) String inspectionNote,

            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {

        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề không được để trống");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new RuntimeException("Mô tả không được để trống");
        }
        if (price == null || price <= 0) {
            throw new RuntimeException("Giá bán phải lớn hơn 0");
        }

        BikePostRequest req = mapper.createRequest(
                title, description, price, status, city, district,
                brand, model, year, frameMaterial, frameSize,
                brakeType, groupset, mileage, categoryId,
                allowNegotiation
        );

        // 🔥 GẮN DỮ LIỆU KIỂM ĐỊNH VÀO REQUEST
        req.setRequestInspection(requestInspection);
        req.setInspectionAddress(inspectionAddress);
        req.setInspectionScheduledDate(inspectionScheduledDate);
        req.setInspectionNote(inspectionNote);

        return service.create(req, images != null ? images : List.of());
    }

    @GetMapping
    @Operation(summary = "Get all approved bike posts with pagination")
    public Page<BikePostResponse> getAll(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sort,

            @Parameter(description = "Sort direction: asc or desc", example = "desc")
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String validSort = validateSortField(sort);
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, validSort));
        return service.getAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get bike post by ID")
    public BikePostResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    @Operation(summary = "Update bike post")
    public BikePostResponse update(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("price") Double price,
            @RequestParam("status") BikeStatus status,

            @RequestParam("city") City city,
            @RequestParam("district") HCMDistrict district,

            @RequestParam("brand") BikeBrand brand,
            @RequestParam(value = "model", required = false) String model,
            @RequestParam(value = "year", required = false) Integer year,

            @RequestParam(value = "frameMaterial", required = false) FrameMaterial frameMaterial,
            @RequestParam(value = "frameSize", required = false) FrameSize frameSize,
            @RequestParam(value = "brakeType", required = false) BrakeType brakeType,
            @RequestParam(value = "groupset", required = false) Groupset groupset,
            @RequestParam(value = "mileage", required = false) Integer mileage,

            @RequestParam("categoryId") Integer categoryId,

            @RequestParam(value = "allowNegotiation", required = false, defaultValue = "false") Boolean allowNegotiation,

            // 🔥 THÊM CÁC THAM SỐ KIỂM ĐỊNH (PHÒNG TRƯỜNG HỢP UPDATE CÓ ĐÍNH KÈM)
            @RequestParam(value = "requestInspection", required = false, defaultValue = "false") Boolean requestInspection,
            @RequestParam(value = "inspectionAddress", required = false) String inspectionAddress,
            @RequestParam(value = "inspectionScheduledDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inspectionScheduledDate,
            @RequestParam(value = "inspectionNote", required = false) String inspectionNote,

            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {

        BikePostRequest req = mapper.createRequest(
                title, description, price, status, city, district,
                brand, model, year, frameMaterial, frameSize,
                brakeType, groupset, mileage, categoryId,
                allowNegotiation
        );

        req.setRequestInspection(requestInspection);
        req.setInspectionAddress(inspectionAddress);
        req.setInspectionScheduledDate(inspectionScheduledDate);
        req.setInspectionNote(inspectionNote);

        return service.update(id, req, images);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete bike post")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/my-posts")
    @Operation(summary = "Get current user's bike posts (all statuses)")
    public Page<BikePostResponse> getMyPosts(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sort,
            @Parameter(description = "Sort direction: asc or desc", example = "desc")
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String validSort = validateSortField(sort);
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, validSort));
        return service.getMyPosts(pageable);
    }

    @GetMapping("/search")
    @Operation(summary = "Search bike posts")
    public Page<BikePostResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        String validSort = validateSortField(sort);
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, validSort));
        return service.search(keyword, minPrice, maxPrice, brand, city, pageable);
    }

    private String validateSortField(String sort) {
        return switch (sort.toLowerCase()) {
            case "id" -> "id";
            case "title" -> "title";
            case "price" -> "price";
            case "createdat", "created_at" -> "createdAt";
            case "updatedat", "updated_at" -> "updatedAt";
            case "poststatus", "post_status" -> "postStatus";
            case "approvedat", "approved_at" -> "approvedAt";
            case "userid", "user_id" -> "userId";
            case "brand" -> "brand";
            case "city" -> "city";
            case "year" -> "year";
            default -> "createdAt"; // Default fallback
        };
    }
}