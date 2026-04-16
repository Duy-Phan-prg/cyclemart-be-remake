package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.CreatePriorityPackageRequest;
import com.example.cyclemartberemake.dto.request.PostPrioritySubscriptionRequest;
import com.example.cyclemartberemake.dto.request.PriorityPackageRequest;
import com.example.cyclemartberemake.dto.response.PostPrioritySubscriptionResponse;
import com.example.cyclemartberemake.dto.response.PriorityPackageResponse;
import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.PriorityLevel;
import com.example.cyclemartberemake.entity.PriorityPackage;
import com.example.cyclemartberemake.entity.PostPrioritySubscription;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.PostPrioritySubscriptionRepository;
import com.example.cyclemartberemake.repository.PriorityPackageRepository;
import com.example.cyclemartberemake.service.PostPrioritySubscriptionService;
import com.example.cyclemartberemake.service.PriorityPackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class PriorityPackageServiceTest {

    @Autowired
    private PriorityPackageService priorityPackageService;

    @Autowired
    private PostPrioritySubscriptionService subscriptionService;

    @Autowired
    private PriorityPackageRepository packageRepository;

    @Autowired
    private BikePostRepository bikePostRepository;

    @Autowired
    private PostPrioritySubscriptionRepository subscriptionRepository;

    private BikePost testPost;
    private PriorityPackage testPackage;

    @BeforeEach
    public void setUp() {
        // Create test bike post
        testPost = BikePost.builder()
                .title("Test Bike")
                .description("Test Description")
                .price(10000000.0)
                .allowNegotiation(false)
                .build();
        testPost = bikePostRepository.save(testPost);

        // Create test priority package
        testPackage = PriorityPackage.builder()
                .name("Test Package")
                .description("Test Priority Package")
                .price(50000.0)
                .durationDays(7)
                .priorityLevel(PriorityLevel.PLATINUM)
                .isActive(true)
                .build();
        testPackage = packageRepository.save(testPackage);
    }

    @Test
    public void testCreatePriorityPackage() {
        CreatePriorityPackageRequest request = new CreatePriorityPackageRequest();
        request.setName("New Package");
        request.setDescription("New Test Package");
        request.setPrice(75000.0);
        request.setDurationDays(14);
        request.setPriorityLevel(PriorityLevel.GOLD);
        request.setIsActive(true);

        PriorityPackageResponse response = priorityPackageService.create(request);

        assertNotNull(response.getId());
        assertEquals("New Package", response.getName());
        assertEquals(75000.0, response.getPrice());
        assertEquals(PriorityLevel.GOLD, response.getPriorityLevel());
    }

    @Test
    public void testUpdatePriorityPackage() {
        PriorityPackageRequest request = new PriorityPackageRequest();
        request.setName("Updated Package");
        request.setDescription("Updated Description");
        request.setPrice(100000.0);
        request.setDurationDays(30);
        request.setPriorityLevel(PriorityLevel.PLATINUM);
        request.setIsActive(true);

        PriorityPackageResponse response = priorityPackageService.update(testPackage.getId(), request);

        assertEquals("Updated Package", response.getName());
        assertEquals(100000.0, response.getPrice());
        assertEquals(30, response.getDurationDays());
    }

    @Test
    public void testGetActivePackages() {
        List<PriorityPackageResponse> responses = priorityPackageService.getActivePackages();

        assertFalse(responses.isEmpty());
        assertTrue(responses.stream().allMatch(PriorityPackageResponse::getIsActive));
    }

    @Test
    public void testSubscribePostToPriority() {
        PostPrioritySubscriptionRequest request = new PostPrioritySubscriptionRequest();
        request.setPostId(testPost.getId());
        request.setPackageId(testPackage.getId());

        PostPrioritySubscriptionResponse response = subscriptionService.subscribe(request);

        assertNotNull(response.getId());
        assertEquals(testPost.getId(), response.getPostId());
        assertEquals(testPackage.getName(), response.getPackageName());
        assertTrue(response.getIsActive());
    }

    @Test
    public void testGetActiveSubscriptionsByPostId() {
        PostPrioritySubscriptionRequest request = new PostPrioritySubscriptionRequest();
        request.setPostId(testPost.getId());
        request.setPackageId(testPackage.getId());

        subscriptionService.subscribe(request);

        List<PostPrioritySubscriptionResponse> subscriptions =
            subscriptionService.getByPostId(testPost.getId());

        assertFalse(subscriptions.isEmpty());
        assertEquals(testPost.getId(), subscriptions.get(0).getPostId());
    }

    @Test
    public void testHasActivePrioritySubscription() {
        PostPrioritySubscriptionRequest request = new PostPrioritySubscriptionRequest();
        request.setPostId(testPost.getId());
        request.setPackageId(testPackage.getId());

        subscriptionService.subscribe(request);

        assertTrue(subscriptionService.hasActivePrioritySubscription(testPost.getId()));
    }

    @Test
    public void testUnsubscribeFromPriority() {
        PostPrioritySubscriptionRequest request = new PostPrioritySubscriptionRequest();
        request.setPostId(testPost.getId());
        request.setPackageId(testPackage.getId());

        PostPrioritySubscriptionResponse subscribed = subscriptionService.subscribe(request);

        subscriptionService.unsubscribe(subscribed.getId());

        List<PostPrioritySubscriptionResponse> subscriptions =
            subscriptionService.getByPostId(testPost.getId());

        assertTrue(subscriptions.isEmpty());
    }

    @Test
    public void testResubscribeAfterUnsubscribe() {
        PostPrioritySubscriptionRequest request = new PostPrioritySubscriptionRequest();
        request.setPostId(testPost.getId());
        request.setPackageId(testPackage.getId());

        // Subscribe
        PostPrioritySubscriptionResponse subscribed1 = subscriptionService.subscribe(request);
        assertTrue(subscribed1.getIsActive());

        // Unsubscribe
        subscriptionService.unsubscribe(subscribed1.getId());

        List<PostPrioritySubscriptionResponse> afterUnsubscribe =
            subscriptionService.getByPostId(testPost.getId());
        assertTrue(afterUnsubscribe.isEmpty());

        // Should be able to resubscribe the same package
        PostPrioritySubscriptionResponse subscribed2 = subscriptionService.subscribe(request);
        assertNotNull(subscribed2.getId());
        assertTrue(subscribed2.getIsActive());

        List<PostPrioritySubscriptionResponse> afterResubscribe =
            subscriptionService.getByPostId(testPost.getId());
        assertFalse(afterResubscribe.isEmpty());
    }

    @Test
    public void testCannotSubscribeTwiceWithoutUnsubscribe() {
        PostPrioritySubscriptionRequest request = new PostPrioritySubscriptionRequest();
        request.setPostId(testPost.getId());
        request.setPackageId(testPackage.getId());

        subscriptionService.subscribe(request);

        // Attempting to subscribe again should throw exception
        assertThrows(RuntimeException.class, () -> {
            subscriptionService.subscribe(request);
        }, "Bài post đã đăng ký gói ưu tiên này");
    }

    @Test
    public void testDuplicateSubscriptionThrowsError() {
        PostPrioritySubscriptionRequest request = new PostPrioritySubscriptionRequest();
        request.setPostId(testPost.getId());
        request.setPackageId(testPackage.getId());

        subscriptionService.subscribe(request);

        // Attempting to subscribe again should throw exception
        assertThrows(RuntimeException.class, () -> {
            subscriptionService.subscribe(request);
        });
    }

    @Test
    public void testDeletePriorityPackage() {
        priorityPackageService.delete(testPackage.getId());

        assertFalse(packageRepository.existsById(testPackage.getId()));
    }

    @Test
    public void testSubscriptionWithInactivePackageThrowsError() {
        testPackage.setIsActive(false);
        packageRepository.save(testPackage);

        PostPrioritySubscriptionRequest request = new PostPrioritySubscriptionRequest();
        request.setPostId(testPost.getId());
        request.setPackageId(testPackage.getId());

        assertThrows(RuntimeException.class, () -> {
            subscriptionService.subscribe(request);
        });
    }
}
