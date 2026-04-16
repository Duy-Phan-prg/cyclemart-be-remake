package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.PostPrioritySubscriptionRequest;
import com.example.cyclemartberemake.dto.response.PostPrioritySubscriptionResponse;
import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.PostPrioritySubscription;
import com.example.cyclemartberemake.entity.PriorityPackage;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.PostPrioritySubscriptionRepository;
import com.example.cyclemartberemake.repository.PriorityPackageRepository;
import com.example.cyclemartberemake.service.PostPrioritySubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PostPrioritySubscriptionServiceImpl implements PostPrioritySubscriptionService {

    private final PostPrioritySubscriptionRepository repository;
    private final BikePostRepository postRepository;
    private final PriorityPackageRepository packageRepository;

    @Override
    @Transactional
    public PostPrioritySubscriptionResponse subscribe(PostPrioritySubscriptionRequest request) {
        // Kiểm tra bài post tồn tại
        BikePost post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Bài post không tồn tại"));

        // Kiểm tra gói ưu tiên tồn tại và hoạt động
        PriorityPackage pkg = packageRepository.findById(request.getPackageId())
                .orElseThrow(() -> new RuntimeException("Gói ưu tiên không tồn tại"));

        if (!pkg.getIsActive()) {
            throw new RuntimeException("Gói ưu tiên không còn hoạt động");
        }

        // Lấy tất cả các subscriptions đang thực sự hoạt động của bài post này (còn hạn và isActive = true)
        List<PostPrioritySubscription> activeSubscriptions = repository.findActiveSubscriptionsByPostId(request.getPostId());

        if (!activeSubscriptions.isEmpty()) {
            PostPrioritySubscription currentActive = activeSubscriptions.get(0);
            if (currentActive.getPriorityPackage().getId().equals(request.getPackageId())) {
                throw new RuntimeException("Bài post đã đăng ký gói ưu tiên này rồi và vẫn đang hoạt động");
            } else {
                throw new RuntimeException("Bài post đang có gói ưu tiên '" + currentActive.getPriorityPackage().getName() + "' đang hoạt động. Vui lòng hủy gói này trước khi đăng ký gói khác");
            }
        }

        // Nếu ko có gói nào đang hoạt động.
        // Kiểm tra xem ĐÃ TỪNG đăng ký gói này chưa để tái sử dụng record
        var existingSubscriptionForThisPackage = repository.findByPostIdAndPriorityPackageId(request.getPostId(), request.getPackageId());

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(pkg.getDurationDays());

        PostPrioritySubscription subscription;

        if (existingSubscriptionForThisPackage.isPresent()) {
            // Reactivate old subscription (nếu đã hủy trước đó)
            subscription = existingSubscriptionForThisPackage.get();
            subscription.setIsActive(true);
            subscription.setStartDate(startDate);
            subscription.setEndDate(endDate);
        } else {
            // Create new subscription
            subscription = PostPrioritySubscription.builder()
                    .post(post)
                    .priorityPackage(pkg)
                    .startDate(startDate)
                    .endDate(endDate)
                    .isActive(true)
                    .build();
        }

        PostPrioritySubscription saved = repository.save(subscription);
        repository.flush(); // Đảm bảo lưu lập tức vào DB
        return toResponse(saved);
    }

    @Override
    @Transactional
    public Map<String, Object> unsubscribe(Long id) {
        PostPrioritySubscription subscription = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đăng ký ưu tiên không tồn tại"));

        if (!subscription.getIsActive()) {
            throw new RuntimeException("Đăng ký ưu tiên này đã bị hủy rồi");
        }

        subscription.setIsActive(false);
        repository.save(subscription);
        repository.flush();  // Ensure changes are flushed to database

        // Return success message
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Đã hủy đăng ký gói ưu tiên thành công");
        response.put("subscriptionId", id);

        return response;
    }

    @Override
    public PostPrioritySubscriptionResponse getById(Long id) {
        PostPrioritySubscription subscription = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Đăng ký ưu tiên không tồn tại"));
        return toResponse(subscription);
    }

    @Override
    public List<PostPrioritySubscriptionResponse> getByPostId(Long postId) {
        List<PostPrioritySubscription> subscriptions = repository.findActiveSubscriptionsByPostId(postId);
        return subscriptions.stream().map(this::toResponse).toList();
    }

    @Override
    public List<PostPrioritySubscriptionResponse> getAllActive() {
        List<PostPrioritySubscription> subscriptions = repository.findActiveSubscriptionsSorted();
        return subscriptions.stream().map(this::toResponse).toList();
    }

    @Override
    public boolean hasActivePrioritySubscription(Long postId) {
        return repository.hasActivePrioritySubscription(postId);
    }

    @Override
    @Transactional
    public Map<String, Object> unsubscribeByPostId(Long postId) {
        List<PostPrioritySubscription> activeSubs = repository.findActiveSubscriptionsByPostId(postId);

        if (activeSubs.isEmpty()) {
            throw new RuntimeException("Bài post không có gói ưu tiên nào đang hoạt động để hủy");
        }

        for (PostPrioritySubscription sub : activeSubs) {
            sub.setIsActive(false);
            repository.save(sub);
        }
        repository.flush();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Đã hủy gói ưu tiên đang hoạt động của bài post thành công");
        response.put("postId", postId);

        return response;
    }

    @Override
    @Transactional
    public void expireExpiredSubscriptions() {
        List<PostPrioritySubscription> subscriptions = repository.findAll();
        LocalDateTime now = LocalDateTime.now();

        subscriptions.stream()
                .filter(sub -> sub.getIsActive() && sub.getEndDate().isBefore(now))
                .forEach(sub -> {
                    sub.setIsActive(false);
                    repository.save(sub);
                });

        repository.flush();
    }

    private PostPrioritySubscriptionResponse toResponse(PostPrioritySubscription subscription) {
        return PostPrioritySubscriptionResponse.builder()
                .id(subscription.getId())
                .postId(subscription.getPost().getId())
                .postTitle(subscription.getPost().getTitle())
                .packageName(subscription.getPriorityPackage().getName())
                .priorityLevel(subscription.getPriorityPackage().getPriorityLevel())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .isActive(subscription.getIsActive())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}
