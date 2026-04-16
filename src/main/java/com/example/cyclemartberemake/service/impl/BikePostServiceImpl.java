package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
import com.example.cyclemartberemake.dto.response.PriorityPackageResponse;
import com.example.cyclemartberemake.entity.BikeImage;
import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.Categories;
import com.example.cyclemartberemake.entity.PostPrioritySubscription;
import com.example.cyclemartberemake.exception.CategoryValidationException;
import com.example.cyclemartberemake.mapper.BikePostMapper;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.CategoryRepository;
import com.example.cyclemartberemake.repository.PostPrioritySubscriptionRepository;
import com.example.cyclemartberemake.service.BikePostService;
import com.example.cyclemartberemake.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BikePostServiceImpl implements BikePostService {

    private final BikePostRepository postRepo;
    private final CategoryRepository categoryRepo;
    private final CloudinaryService cloudinaryService;
    private final BikePostMapper bikePostMapper;
    private final PostPrioritySubscriptionRepository prioritySubRepo;

    @Override
    public BikePostResponse create(BikePostRequest req, List<MultipartFile> files) {

        // Validate brand
        if (req.getBrand() == null || req.getBrand().trim().isEmpty()) {
            throw new RuntimeException("Thương hiệu không được để trống");
        }

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

        // 4. Map DTO to Entity using MapStruct
        BikePost post = bikePostMapper.toEntity(req);
        post.setCategory(category);
        post.setCreatedAt(LocalDateTime.now());

        BikePost savedPost = postRepo.save(post);

        // 5. Handle image uploads (if any)
        if (files != null && !files.isEmpty()) {
            // ✅ FIX: Thay .toList() → .collect(Collectors.toList())
            List<BikeImage> images = files.stream().map(file -> {
                String url = cloudinaryService.upload(file);
                return BikeImage.builder()
                        .url(url)
                        .post(savedPost)
                        .build();
            }).collect(Collectors.toList());  // ✅ CHANGED

            savedPost.setImages(images);
            postRepo.save(savedPost);
        }

        // 6. Map Entity to Response DTO using MapStruct
        BikePostResponse response = bikePostMapper.toResponse(savedPost);

        // Set images manually (since mapper ignores it)
        response.setImages(
                savedPost.getImages() != null
                        ? savedPost.getImages().stream().map(BikeImage::getUrl).collect(Collectors.toList())  // ✅ CHANGED
                        : new ArrayList<>()  // ✅ Thay List.of() → new ArrayList<>()
        );

        // Set active priority package info
        setActivePriorityInfo(response, savedPost.getId());

        return response;
    }

    @Override
    public List<BikePostResponse> getAll() {
        List<BikePost> posts = postRepo.findAll();
        List<BikePostResponse> responses = bikePostMapper.toResponseList(posts);

        // Set images for each response
        for (int i = 0; i < posts.size(); i++) {
            BikePost post = posts.get(i);
            BikePostResponse response = responses.get(i);

            response.setImages(
                    post.getImages() != null
                            ? post.getImages().stream().map(BikeImage::getUrl).collect(Collectors.toList())  // ✅ CHANGED
                            : new ArrayList<>()  // ✅ CHANGED
            );

            // Set active priority package info
            setActivePriorityInfo(response, post.getId());
        }

        return responses;
    }

    @Override
    public BikePostResponse getById(Long id) {
        BikePost post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        BikePostResponse response = bikePostMapper.toResponse(post);

        // Set images
        response.setImages(
                post.getImages() != null
                        ? post.getImages().stream().map(BikeImage::getUrl).collect(Collectors.toList())  // ✅ CHANGED
                        : new ArrayList<>()  // ✅ CHANGED
        );

        // Set active priority package info
        setActivePriorityInfo(response, post.getId());

        return response;
    }

    private void setActivePriorityInfo(BikePostResponse response, Long postId) {
        List<PostPrioritySubscription> activeSubs = prioritySubRepo.findActiveSubscriptionsByPostId(postId);

        if (!activeSubs.isEmpty()) {
            // Lấy gói có mức ưu tiên cao nhất
            PostPrioritySubscription highestPriority = activeSubs.stream()
                    .max((a, b) -> a.getPriorityPackage().getPriorityLevel()
                            .compareTo(b.getPriorityPackage().getPriorityLevel()))
                    .orElse(null);

            if (highestPriority != null) {
                response.setActivePriority(
                        PriorityPackageResponse.builder()
                                .id(highestPriority.getPriorityPackage().getId())
                                .name(highestPriority.getPriorityPackage().getName())
                                .description(highestPriority.getPriorityPackage().getDescription())
                                .price(highestPriority.getPriorityPackage().getPrice())
                                .durationDays(highestPriority.getPriorityPackage().getDurationDays())
                                .priorityLevel(highestPriority.getPriorityPackage().getPriorityLevel())
                                .isActive(highestPriority.getIsActive())
                                .createdAt(highestPriority.getPriorityPackage().getCreatedAt())
                                .updatedAt(highestPriority.getPriorityPackage().getUpdatedAt())
                                .build()
                );
            }
        }
    }
}