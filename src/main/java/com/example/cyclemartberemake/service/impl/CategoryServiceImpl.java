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
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
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

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getCategoryTree() {
        // 1. Chỉ lấy các danh mục đang hoạt động (isActive = true)
        List<Categories> activeCategories = categoryRepository.findByIsActive(true);

        // 2. Map sang DTO
        List<CategoryResponseDTO> allDtos = activeCategories.stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());

        // 3. Đưa vào Map để tra cứu nhanh bằng ID
        Map<Integer, CategoryResponseDTO> dtoMap = allDtos.stream()
                .collect(Collectors.toMap(CategoryResponseDTO::getId, dto -> dto));

        List<CategoryResponseDTO> rootCategories = new ArrayList<>();

        // 4. Lắp ráp cây
        for (CategoryResponseDTO dto : allDtos) {
            if (dto.getParentId() == null) {
                // Nếu không có parent -> Nó là danh mục gốc
                rootCategories.add(dto);
            } else {
                // Nếu có parent -> Tìm parent trong Map và nhét nó vào danh sách children của parent
                CategoryResponseDTO parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(dto);
                }
            }
        }

        return rootCategories;
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

        // Kiểm tra có danh mục con không
        if (categoryRepository.existsByParentId(categoryId)) {
            throw new CategoryValidationException("Không thể xóa danh mục có danh mục con. Vui lòng xóa danh mục con trước.");
        }

        // Kiểm tra có bike posts nào đang sử dụng category này không
        if (categoryRepository.countBikePostsByCategory(categoryId) > 0) {
            throw new CategoryValidationException("Không thể xóa danh mục đang có bài đăng. Vui lòng di chuyển hoặc xóa các bài đăng trước.");
        }
    }
}