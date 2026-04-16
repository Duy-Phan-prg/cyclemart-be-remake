package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.BrandRequestDTO;
import com.example.cyclemartberemake.dto.response.BrandResponseDTO;
import com.example.cyclemartberemake.service.BrandService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Tag(name = "Brand Management", description = "APIs for brand management")
public class BrandController {

    private final BrandService brandService;

    @PostMapping
    @Operation(summary = "Create new brand")
    public ResponseEntity<BrandResponseDTO> create(@Valid @RequestBody BrandRequestDTO request) {
        return ResponseEntity.ok(brandService.createBrand(request));
    }

    @GetMapping
    @Operation(summary = "Get all brands")
    public ResponseEntity<List<BrandResponseDTO>> getAll() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<BrandResponseDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update brand")
    public ResponseEntity<BrandResponseDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody BrandRequestDTO request) {
        return ResponseEntity.ok(brandService.updateBrand(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete brand")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}