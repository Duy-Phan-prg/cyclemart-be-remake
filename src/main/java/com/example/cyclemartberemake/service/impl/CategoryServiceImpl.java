package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.CategoryRequestDTO;
import com.example.cyclemartberemake.dto.response.CategoryResponseDTO;
import com.example.cyclemartberemake.entity.Categories;
import com.example.cyclemartberemake.exception.CategoryValidationException;
import com.example.cyclemartberemake.mapper.CategoryMapper;
import com.example.cyclemartberemake.repository.CategoryRepository;
import com.example.cyclemartberemake.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        validateCreateCategory(request);

        Categories parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryValidationException("Danh mục cha không tồn tại"));
        }

        Categories category = categoryMapper.toEntity(request);
        category.setParent(parent);

        Categories saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Integer id) {
        Categories category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryValidationException("Danh mục không tồn tại"));
        
        return categoryMapper.toResponse(category);
    }

    @Override
    public CategoryResponseDTO updateCategory(Integer id, CategoryRequestDTO request) {
        validateUpdateCategory(id, request);

        Categories category = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryValidationException("Danh mục không tồn tại"));

        Categories parent = null;
        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new CategoryValidationException("Danh mục cha không tồn tại"));
        }

        categoryMapper.updateEntity(request, category);
        category.setParent(parent);

        Categories saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    public void deleteCategory(Integer id) {
        validateDeleteCategory(id);

        categoryRepository.deleteById(id);
    }

    private void validateCreateCategory(CategoryRequestDTO request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new CategoryValidationException("Tên danh mục '" + request.getName() + "' đã tồn tại");
        }

        validateParentCategory(request.getParentId(), null);
    }

    private void validateUpdateCategory(Integer categoryId, CategoryRequestDTO request) {
        Categories existingCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryValidationException("Danh mục không tồn tại"));

        if (categoryRepository.existsByNameAndIdNot(request.getName(), categoryId)) {
            throw new CategoryValidationException("Tên danh mục '" + request.getName() + "' đã tồn tại");
        }

        validateParentCategory(request.getParentId(), categoryId);

        if (request.getParentId() != null) {
            validateNotCircularReference(categoryId, request.getParentId());
        }
    }

    private void validateParentCategory(Integer parentId, Integer currentCategoryId) {
        if (parentId != null) {
            Categories parent = categoryRepository.findById(parentId)
                    .orElseThrow(() -> new CategoryValidationException("Danh mục cha không tồn tại"));

            if (!parent.getIsActive()) {
                throw new CategoryValidationException("Danh mục cha đã bị vô hiệu hóa");
            }

            if (currentCategoryId != null && parentId.equals(currentCategoryId)) {
                throw new CategoryValidationException("Không thể set danh mục cha là chính nó");
            }
        }
    }

    private void validateNotCircularReference(Integer categoryId, Integer parentId) {
        Categories parent = categoryRepository.findById(parentId).orElse(null);
        
        while (parent != null) {
            if (parent.getId().equals(categoryId)) {
                throw new CategoryValidationException("Không thể set danh mục cha là danh mục con của chính nó");
            }
            parent = parent.getParent();
        }
    }

    private void validateDeleteCategory(Integer categoryId) {
        Categories category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryValidationException("Danh mục không tồn tại"));

        if (categoryRepository.existsByParentId(categoryId)) {
            throw new CategoryValidationException("Không thể xóa danh mục có danh mục con. Vui lòng xóa danh mục con trước.");
        }

    }
}