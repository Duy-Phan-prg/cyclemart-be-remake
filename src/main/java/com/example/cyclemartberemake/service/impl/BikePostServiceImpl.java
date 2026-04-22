package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
import com.example.cyclemartberemake.dto.response.PriorityPackageResponse;
import com.example.cyclemartberemake.entity.*;
import com.example.cyclemartberemake.entity.PostStatus;
import com.example.cyclemartberemake.exception.CategoryValidationException;
import com.example.cyclemartberemake.mapper.BikePostMapper;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.CategoryRepository;
import com.example.cyclemartberemake.repository.PostPrioritySubscriptionRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.BikePostService;
import com.example.cyclemartberemake.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final BikePostMapper mapper;
    private final PostPrioritySubscriptionRepository priorityRepo;
    private final UserRepository userRepository;

    // ================= CREATE =================
    @Override
    public BikePostResponse create(BikePostRequest req, List<MultipartFile> files) {

        Categories category = validateCategory(req.getCategoryId());

        BikePost post = mapper.toEntity(req);
        post.setCategory(category);

        Long userId = getCurrentUserId();
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        post.setUser(user);
        post.setUserId(user.getId());
        post.setPostStatus(PostStatus.PENDING);
        post.setCreatedAt(LocalDateTime.now());

        BikePost savedPost = postRepo.save(post);

        handleImages(savedPost, files);

        return buildResponse(savedPost);
    }

    // ================= GET ALL =================
    @Override
    public Page<BikePostResponse> getAll(Pageable pageable) {
        // 🔥 Đã SỬ DỤNG QUERY SẮP XẾP THEO ƯU TIÊN (Priority > Date)
        Page<BikePost> posts = postRepo.findApprovedPostsWithPriority(PostStatus.APPROVED, pageable);
        return posts.map(this::buildResponse);
    }

    // ================= GET BY ID =================
    @Override
    public BikePostResponse getById(Long id) {
        BikePost post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        post.setViewCount((post.getViewCount() == null ? 0 : post.getViewCount()) + 1);
        post = postRepo.save(post);
        return buildResponse(post);
    }

    // ================= UPDATE =================
    @Override
    public BikePostResponse update(Long id, BikePostRequest req, List<MultipartFile> files) {
        BikePost post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        // Check ownership
        Long currentUserId = getCurrentUserId();
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài đăng này");
        }

        Categories category = validateCategory(req.getCategoryId());

        // Update fields
        post.setTitle(req.getTitle());
        post.setDescription(req.getDescription());
        post.setPrice(req.getPrice());
        post.setStatus(req.getStatus());
        post.setCity(req.getCity());
        post.setDistrict(req.getDistrict());
        post.setBrand(req.getBrand());
        post.setModel(req.getModel());
        post.setYear(req.getYear());
        post.setFrameMaterial(req.getFrameMaterial());
        post.setFrameSize(req.getFrameSize());
        post.setBrakeType(req.getBrakeType());
        post.setGroupset(req.getGroupset());
        post.setMileage(req.getMileage());
        post.setCategory(category);
        post.setAllowNegotiation(req.getAllowNegotiation());

        post.setPostStatus(PostStatus.PENDING);
        post.setApprovedBy(null);
        post.setApprovedAt(null);
        post.setRejectionReason(null);
        post.setUpdatedAt(LocalDateTime.now());

        BikePost savedPost = postRepo.save(post);

        // Handle new images if provided
        if (files != null && !files.isEmpty()) {
            handleImages(savedPost, files);
        }

        return buildResponse(savedPost);
    }

    // ================= DELETE =================
    @Override
    public void delete(Long id) {
        BikePost post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        Long currentUserId = getCurrentUserId();
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền xóa bài đăng này");
        }

        postRepo.delete(post);
    }

    // ================= GET MY POSTS =================
    @Override
    public Page<BikePostResponse> getMyPosts(Pageable pageable) {
        Long currentUserId = getCurrentUserId();
        Page<BikePost> posts = postRepo.findByUserId(currentUserId, pageable);
        return posts.map(this::buildResponse);
    }

    // ================= SEARCH =================
    @Override
    public Page<BikePostResponse> search(String keyword, Double minPrice, Double maxPrice,
                                         String brand, String city, Pageable pageable) {
        // 🔥  SỬ DỤNG QUERY TÌM KIẾM CÓ SẮP XẾP THEO ƯU TIÊN (Priority > Date)
        Page<BikePost> posts = postRepo.searchPostsWithPriority(keyword, minPrice, maxPrice, brand, city, pageable);
        return posts.map(this::buildResponse);
    }

    // ================= ADMIN =================
    @Override
    public void approve(Long id) {
        BikePost post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài"));

        post.setPostStatus(PostStatus.APPROVED);
        post.setApprovedAt(LocalDateTime.now());
        post.setApprovedBy(getCurrentUserId());
        post.setRejectionReason(null);

        postRepo.save(post);
    }

    @Override
    public void reject(Long id, String reason) {
        BikePost post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài"));

        post.setPostStatus(PostStatus.REJECTED);
        post.setRejectionReason(reason);
        post.setApprovedBy(null);
        post.setApprovedAt(null);

        postRepo.save(post);
    }

    @Override
    public Page<BikePostResponse> getAllForAdmin(Pageable pageable) {
        Page<BikePost> posts = postRepo.findAll(pageable);
        return posts.map(this::buildResponse);
    }

    // ================= HELPER =================

    private Categories validateCategory(Integer id) {
        Categories category = categoryRepo.findById(id)
                .orElseThrow(() -> new CategoryValidationException("Danh mục không tồn tại"));

        if (!category.getIsActive()) {
            throw new CategoryValidationException("Danh mục đã bị vô hiệu hóa");
        }

        if (categoryRepo.existsByParentId(category.getId())) {
            throw new CategoryValidationException("Chỉ được chọn danh mục con");
        }

        return category;
    }

    private void handleImages(BikePost post, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        List<BikeImage> images = files.stream().map(file -> {
            String url = cloudinaryService.upload(file);

            return BikeImage.builder()
                    .url(url)
                    .post(post)
                    .build();
        }).toList();

        post.setImages(images);
        postRepo.save(post);
    }

    private BikePostResponse buildResponse(BikePost post) {

        BikePostResponse response = mapper.toResponse(post);

        if (post.getPostStatus() != null) {
            response.setPostStatus(post.getPostStatus().name());
        }

        response.setIsVerified(post.getIsVerified());

        if (response.getUserId() == null && post.getUserId() != null) {
            response.setUserId(post.getUserId());
        }

        Users seller = post.getUser();
        if (seller == null && post.getUserId() != null) {
            seller = userRepository.findById(post.getUserId()).orElse(null);
        }

        if (seller != null) {
            response.setUserId(seller.getId());
            response.setSellerName(seller.getFullName());
            response.setSellerEmail(seller.getEmail());
        }

        setActivePriorityInfo(response, post.getId());

        return response;
    }

    private void setActivePriorityInfo(BikePostResponse response, Long postId) {

        List<PostPrioritySubscription> activeSubs =
                priorityRepo.findActiveSubscriptionsByPostId(postId);

        if (activeSubs.isEmpty()) return;

        PostPrioritySubscription highest = activeSubs.stream()
                .max((a, b) -> a.getPriorityPackage().getPriorityLevel()
                        .compareTo(b.getPriorityPackage().getPriorityLevel()))
                .orElse(null);

        if (highest != null) {
            response.setActivePriority(
                    PriorityPackageResponse.builder()
                            .id(highest.getPriorityPackage().getId())
                            .name(highest.getPriorityPackage().getName())
                            .description(highest.getPriorityPackage().getDescription())
                            .price(highest.getPriorityPackage().getPrice())
                            .durationDays(highest.getPriorityPackage().getDurationDays())
                            .priorityLevel(highest.getPriorityPackage().getPriorityLevel())
                            .isActive(highest.getIsActive())
                            .createdAt(highest.getPriorityPackage().getCreatedAt())
                            .updatedAt(highest.getPriorityPackage().getUpdatedAt())
                            .build()
            );
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Users user) {
            return (long) user.getId();
        }
        throw new RuntimeException("Người dùng chưa đăng nhập");
    }

}
