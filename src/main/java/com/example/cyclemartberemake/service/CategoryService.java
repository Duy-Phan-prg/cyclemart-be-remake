package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.CategoryRequestDTO;
import com.example.cyclemartberemake.dto.response.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    CategoryResponseDTO createCategory(CategoryRequestDTO request);
    List<CategoryResponseDTO> getAllCategories();
    CategoryResponseDTO getCategoryById(Integer id);
    CategoryResponseDTO updateCategory(Integer id, CategoryRequestDTO request);
    void deleteCategory(Integer id);
    List<CategoryResponseDTO> getCategoryTree();
}