package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.CreatePriorityPackageRequest;
import com.example.cyclemartberemake.dto.request.PriorityPackageRequest;
import com.example.cyclemartberemake.dto.response.PriorityPackageResponse;
import com.example.cyclemartberemake.service.PriorityPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/priority-packages")
@RequiredArgsConstructor
@Tag(name = "Priority Package Management", description = "APIs for managing priority packages")
public class PriorityPackageController {

    private final PriorityPackageService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new priority package")
    public PriorityPackageResponse create(@Valid @RequestBody CreatePriorityPackageRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a priority package (partial update supported)")
    public PriorityPackageResponse update(
            @PathVariable Long id,
            @Valid @RequestBody PriorityPackageRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a priority package")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get priority package by ID")
    public PriorityPackageResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping
    @Operation(summary = "Get all priority packages")
    public List<PriorityPackageResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active priority packages")
    public List<PriorityPackageResponse> getActivePackages() {
        return service.getActivePackages();
    }
}
