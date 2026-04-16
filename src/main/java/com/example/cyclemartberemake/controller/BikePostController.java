package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
import com.example.cyclemartberemake.entity.*;
import com.example.cyclemartberemake.mapper.BikePostMapper;
import com.example.cyclemartberemake.service.BikePostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        // Validate required fields
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("Tiêu đề không được để trống");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new RuntimeException("Mô tả không được để trống");
        }
        if (price == null || price <= 0) {
            throw new RuntimeException("Giá bán phải lớn hơn 0");
        }
        
        BikePostRequest req = mapper.createRequest(title, description, price, status, city, district, 
                                                 brand, model, year, frameMaterial, frameSize, 
                                                 brakeType, groupset, mileage, categoryId);
        
        return service.create(req, images != null ? images : List.of());
    }

    @GetMapping
    @Operation(summary = "Get all bike posts")
    public List<BikePostResponse> getAll() {
        return service.getAll();
    }
}