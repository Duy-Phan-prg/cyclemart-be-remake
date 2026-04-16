package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.BrandRequestDTO;
import com.example.cyclemartberemake.dto.response.BrandResponseDTO;

import java.util.List;

public interface BrandService {

    List<BrandResponseDTO> getAllBrands();

    BrandResponseDTO getBrandById(Integer id);

    BrandResponseDTO createBrand(BrandRequestDTO request);

    BrandResponseDTO updateBrand(Integer id, BrandRequestDTO request);

    void deleteBrand(Integer id);
}