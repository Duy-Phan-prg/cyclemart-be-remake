package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
import com.example.cyclemartberemake.entity.BikeImage;
import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.Categories;
import com.example.cyclemartberemake.exception.CategoryValidationException;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.CategoryRepository;
import com.example.cyclemartberemake.service.BikePostService;
import com.example.cyclemartberemake.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BikePostServiceImpl implements BikePostService {

    private final BikePostRepository postRepo;
    private final CategoryRepository categoryRepo;
    private final CloudinaryService cloudinaryService;

    @Override
    public BikePostResponse create(BikePostRequest req, List<MultipartFile> files) {

        // 1. Validate category exists
        Categories category = categoryRepo.findById(req.getCategoryId())
                .orElseThrow(() -> new CategoryValidationException("Danh mục không tồn tại"));

        // 2. Validate category is active
        if (!category.getIsActive()) {
            throw new CategoryValidationException("Danh mục đã bị vô hiệu hóa");
        }

        // 3. Validate category is leaf (không có con) - CHỈ CHO PHÉP CATEGORY CON
        if (categoryRepo.existsByParentId(category.getId())) {
            throw new CategoryValidationException("Chỉ được chọn danh mục con (không có danh mục con bên trong). Vui lòng chọn danh mục cụ thể hơn.");
        }

        BikePost post = BikePost.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .price(req.getPrice())
                .status(req.getStatus())
                .city(req.getCity())
                .district(req.getDistrict())
                .brand(req.getBrand())
                .model(req.getModel())
                .category(category)
                .createdAt(LocalDateTime.now())
                .build();

        BikePost savedPost = postRepo.save(post);

        List<BikeImage> images = files.stream().map(file -> {
            String url = cloudinaryService.upload(file);

            return BikeImage.builder()
                    .url(url)
                    .post(savedPost)
                    .build();
        }).toList();

        savedPost.setImages(images);
        postRepo.save(savedPost);

        return mapToResponse(post);
    }

    @Override
    public List<BikePostResponse> getAll() {
        return postRepo.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private BikePostResponse mapToResponse(BikePost post) {
        return BikePostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .price(post.getPrice())
                .categoryName(post.getCategory().getName())
                .images(
                        post.getImages() != null
                                ? post.getImages().stream().map(BikeImage::getUrl).toList()
                                : List.of()
                )
                .build();
    }
}