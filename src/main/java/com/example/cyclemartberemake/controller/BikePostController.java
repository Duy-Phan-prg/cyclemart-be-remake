package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
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

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Create new bike post")
    public BikePostResponse create(
            @RequestPart("data") @Valid BikePostRequest req,
            @RequestPart("images") List<MultipartFile> images
    ) {
        return service.create(req, images);
    }

    @GetMapping
    @Operation(summary = "Get all bike posts")
    public List<BikePostResponse> getAll() {
        return service.getAll();
    }
}