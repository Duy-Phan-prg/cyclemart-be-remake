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
import com.example.cyclemartberemake.repository.InspectionRepository;
import com.example.cyclemartberemake.repository.PostPrioritySubscriptionRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.BikePostService;
import com.example.cyclemartberemake.service.CloudinaryService;
import com.example.cyclemartberemake.service.UserService;
import com.example.cyclemartberemake.service.PaymentNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final InspectionRepository inspectionRepo;

    // 🔥 Đã thêm 2 dòng này để sửa lỗi thiếu Service khi hoàn tiền kiểm định
    private final UserService userService;
    private final PaymentNotificationService notificationService;

    // ================= CREATE =================
    @Override
    @Transactional
    public BikePostResponse create(BikePostRequest req, List<MultipartFile> files) {
        Categories category = validateCategory(req.getCategoryId());

        // Validate year
        int currentYear = java.time.Year.now().getValue();
        if (req.getYear() != null && req.getYear() > currentYear) {
            throw new RuntimeException("Năm sản xuất không được vượt quá năm hiện tại (" + currentYear + ")");
        }

        BikePost post = mapper.toEntity(req);
        post.setCategory(category);

        Long userId = getCurrentUserId();
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        post.setUser(user);
        post.setUserId(user.getId());
        post.setIsPriority(false);
        post.setIsVerified(false);
        post.setPostStatus(PostStatus.PENDING);
        post.setCreatedAt(LocalDateTime.now());

        if (Boolean.TRUE.equals(req.getRequestInspection())) {
            post.setIsRequestedInspection(true);
            post.setInspectionAddress(req.getInspectionAddress());
            post.setInspectionScheduledDate(req.getInspectionScheduledDate());
            post.setInspectionNote(req.getInspectionNote());
        } else {
            post.setIsRequestedInspection(false);
        }

        BikePost savedPost = postRepo.save(post);
        handleImages(savedPost, files);

        return buildResponse(savedPost);
    }

    // ================= GET ALL =================
    @Override
    public Page<BikePostResponse> getAll(Pageable pageable) {
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
    @Transactional
    public BikePostResponse update(Long id, BikePostRequest req, List<MultipartFile> files) {
        BikePost post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        Long currentUserId = getCurrentUserId();
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa bài đăng này");
        }

        Categories category = validateCategory(req.getCategoryId());

        // Validate year
        int currentYear = java.time.Year.now().getValue();
        if (req.getYear() != null && req.getYear() > currentYear) {
            throw new RuntimeException("Năm sản xuất không được vượt quá năm hiện tại (" + currentYear + ")");
        }

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
        post.setCategory(category);
        post.setAllowNegotiation(req.getAllowNegotiation());

        post.setPostStatus(PostStatus.PENDING);
        post.setApprovedBy(null);
        post.setApprovedAt(null);
        post.setRejectionReason(null);
        post.setUpdatedAt(LocalDateTime.now());

        BikePost savedPost = postRepo.save(post);

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

    @Override
    public void cancelPost(Long id) {
        BikePost post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        Long currentUserId = getCurrentUserId();
        if (!post.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("Bạn không có quyền hủy bài đăng này");
        }

        if (!post.getPostStatus().equals(PostStatus.PENDING)) {
            throw new RuntimeException("Chỉ có thể hủy bài đăng đang chờ duyệt");
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

    @Override
    public Page<BikePostResponse> getPostsByUserId(Long userId, Pageable pageable) {
        Page<BikePost> posts = postRepo.findByUserId(userId, pageable);
        List<BikePostResponse> responses = posts.getContent().stream()
                .filter(post -> post.getPostStatus() == PostStatus.APPROVED)
                .map(this::buildResponse)
                .toList();
        return new PageImpl<>(responses, pageable, responses.size());
    }

    // ================= SEARCH =================
    @Override
    public Page<BikePostResponse> search(String keyword, Double minPrice, Double maxPrice,
                                         String brand, String city, Pageable pageable) {
        Page<BikePost> posts = postRepo.searchPostsWithPriority(keyword, minPrice, maxPrice, brand, city, pageable);
        return posts.map(this::buildResponse);
    }

    // ================= ADMIN =================
    @Override
    @Transactional
    public void approve(Long id) {
        BikePost post = postRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài"));

        post.setPostStatus(PostStatus.APPROVED);
        post.setApprovedAt(LocalDateTime.now());
        post.setApprovedBy(getCurrentUserId());
        post.setRejectionReason(null);

        if (Boolean.TRUE.equals(post.getIsRequestedInspection())) {
            Inspection inspection = Inspection.builder()
                    .bikePost(post)
                    .seller(post.getUser())
                    .address(post.getInspectionAddress())
                    .scheduledDateTime(post.getInspectionScheduledDate())
                    .note(post.getInspectionNote())
                    .status(InspectionStatus.PENDING)
                    .build();
            inspectionRepo.save(inspection);

            post.setIsRequestedInspection(false);
        }

        postRepo.save(post);
    }

    // 🔥 ĐÃ FIX LỖI Ở HÀM NÀY: Dùng đúng tên postRepo và các service đã inject
    @Transactional
    public void reject(Long postId, String reason) {
        BikePost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy bài đăng"));

        post.setPostStatus(PostStatus.REJECTED);
        post.setRejectionReason(reason);

        // HOÀN POINT: Nếu bài này đã đóng phí kiểm định
        if (Boolean.TRUE.equals(post.getIsRequestedInspection())) {
            post.setIsRequestedInspection(false);

            // Giả sử phí kiểm định là 100.000 VNĐ -> Hoàn lại 100 điểm (bạn có thể thay đổi số này)
            int refundPoints = 100;

            try {
                userService.addPoint(post.getUser().getId(), refundPoints);

                notificationService.sendRealTimeNotification(post.getUser().getId(),
                        "Bài đăng bị từ chối do: " + reason + ". Phí kiểm định đã được hoàn " + refundPoints + " điểm.",
                        "INSPECTION_REFUND"
                );
            } catch (Exception e) {
                // Log lỗi nếu hoàn tiền thất bại nhưng vẫn để cho bài bị reject
            }
        }

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

        List<BikeImage> images = new ArrayList<>();
        for (MultipartFile file : files) {
            try {
                String url = cloudinaryService.upload(file);
                BikeImage image = BikeImage.builder()
                        .url(url)
                        .post(post)
                        .build();
                images.add(image);
                System.out.println("✅ Uploaded: " + file.getOriginalFilename() + " -> " + url);
            } catch (Exception e) {
                System.err.println("❌ Failed to upload: " + file.getOriginalFilename() + " - " + e.getMessage());
            }
        }

        if (!images.isEmpty()) {
            post.setImages(images);
            postRepo.save(post);
        }
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