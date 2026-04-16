package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.CreatePriorityPackageRequest;
import com.example.cyclemartberemake.dto.request.PriorityPackageRequest;
import com.example.cyclemartberemake.dto.response.PriorityPackageResponse;
import com.example.cyclemartberemake.entity.PriorityPackage;
import com.example.cyclemartberemake.repository.PriorityPackageRepository;
import com.example.cyclemartberemake.service.PriorityPackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PriorityPackageServiceImpl implements PriorityPackageService {

    private final PriorityPackageRepository repository;

    @Override
    public PriorityPackageResponse create(CreatePriorityPackageRequest request) {
        // Kiểm tra tên gói đã tồn tại
        if (repository.existsByName(request.getName())) {
            throw new RuntimeException("Tên gói ưu tiên đã tồn tại");
        }

        PriorityPackage pkg = PriorityPackage.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .durationDays(request.getDurationDays())
                .priorityLevel(request.getPriorityLevel())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        PriorityPackage saved = repository.save(pkg);
        return toResponse(saved);
    }

    @Override
    public PriorityPackageResponse update(Long id, PriorityPackageRequest request) {
        PriorityPackage pkg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gói ưu tiên không tồn tại"));

        // Chỉ update nếu field không null
        if (request.getName() != null && !request.getName().isEmpty()) {
            // Kiểm tra tên nếu thay đổi
            if (!pkg.getName().equals(request.getName()) && repository.existsByName(request.getName())) {
                throw new RuntimeException("Tên gói ưu tiên đã tồn tại");
            }
            pkg.setName(request.getName());
        }

        if (request.getDescription() != null) {
            pkg.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            pkg.setPrice(request.getPrice());
        }

        if (request.getDurationDays() != null) {
            pkg.setDurationDays(request.getDurationDays());
        }

        if (request.getPriorityLevel() != null) {
            pkg.setPriorityLevel(request.getPriorityLevel());
        }

        if (request.getIsActive() != null) {
            pkg.setIsActive(request.getIsActive());
        }

        PriorityPackage updated = repository.save(pkg);
        return toResponse(updated);
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Gói ưu tiên không tồn tại");
        }
        repository.deleteById(id);
    }

    @Override
    public PriorityPackageResponse getById(Long id) {
        PriorityPackage pkg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gói ưu tiên không tồn tại"));
        return toResponse(pkg);
    }

    @Override
    public List<PriorityPackageResponse> getAll() {
        List<PriorityPackage> packages = repository.findAll();
        return packages.stream().map(this::toResponse).toList();
    }

    @Override
    public List<PriorityPackageResponse> getActivePackages() {
        List<PriorityPackage> packages = repository.findByIsActiveTrue();
        return packages.stream().map(this::toResponse).toList();
    }

    private PriorityPackageResponse toResponse(PriorityPackage pkg) {
        return PriorityPackageResponse.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .description(pkg.getDescription())
                .price(pkg.getPrice())
                .durationDays(pkg.getDurationDays())
                .priorityLevel(pkg.getPriorityLevel())
                .isActive(pkg.getIsActive())
                .createdAt(pkg.getCreatedAt())
                .updatedAt(pkg.getUpdatedAt())
                .build();
    }
}
