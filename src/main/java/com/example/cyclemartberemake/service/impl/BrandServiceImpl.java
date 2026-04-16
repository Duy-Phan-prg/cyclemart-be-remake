package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.BrandRequestDTO;
import com.example.cyclemartberemake.dto.response.BrandResponseDTO;
import com.example.cyclemartberemake.entity.Brand;
import com.example.cyclemartberemake.repository.BrandRepository;
import com.example.cyclemartberemake.service.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;

    @Override
    public List<BrandResponseDTO> getAllBrands() {
        return brandRepository.findAllByOrderByNameAsc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BrandResponseDTO getBrandById(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại"));
        return convertToDTO(brand);
    }

    @Override
    public BrandResponseDTO createBrand(BrandRequestDTO request) {
        if (brandRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Tên thương hiệu đã tồn tại");
        }

        Brand brand = new Brand();
        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());

        return convertToDTO(brandRepository.save(brand));
    }

    @Override
    public BrandResponseDTO updateBrand(Integer id, BrandRequestDTO request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại"));

        if (!brand.getName().equals(request.getName()) &&
                brandRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Tên thương hiệu đã tồn tại");
        }

        brand.setName(request.getName());
        brand.setDescription(request.getDescription());
        brand.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        brand.setUpdatedAt(LocalDateTime.now());

        return convertToDTO(brandRepository.save(brand));
    }

    @Override
    public void deleteBrand(Integer id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Thương hiệu không tồn tại"));
        brandRepository.delete(brand);
    }

    private BrandResponseDTO convertToDTO(Brand brand) {
        return new BrandResponseDTO(
                brand.getId(),
                brand.getName(),
                brand.getDescription(),
                brand.getIsActive(),
                brand.getCreatedAt(),
                brand.getUpdatedAt()
        );
    }
}