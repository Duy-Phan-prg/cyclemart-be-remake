package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.dto.request.PostPrioritySubscriptionRequest;
import com.example.cyclemartberemake.dto.response.PostPrioritySubscriptionResponse;
import com.example.cyclemartberemake.service.PostPrioritySubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/post-priority-subscriptions")
@RequiredArgsConstructor
@Tag(name = "Post Priority Subscription Management", description = "APIs for managing post priority subscriptions")
public class PostPrioritySubscriptionController {

    private final PostPrioritySubscriptionService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Subscribe a post to a priority package")
    public PostPrioritySubscriptionResponse subscribe(
            @Valid @RequestBody PostPrioritySubscriptionRequest request) {
        return service.subscribe(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Unsubscribe a post from priority package by subscription ID")
    public Map<String, Object> unsubscribe(@PathVariable Long id) {
        return service.unsubscribe(id);
    }

    @DeleteMapping("/post/{postId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Unsubscribe a post from its active priority package by post ID")
    public Map<String, Object> unsubscribeByPostId(@PathVariable Long postId) {
        return service.unsubscribeByPostId(postId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription by ID")
    public PostPrioritySubscriptionResponse getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/post/{postId}")
    @Operation(summary = "Get active priority subscriptions for a post")
    public List<PostPrioritySubscriptionResponse> getByPostId(@PathVariable Long postId) {
        return service.getByPostId(postId);
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active priority subscriptions sorted by priority level")
    public List<PostPrioritySubscriptionResponse> getAllActive() {
        return service.getAllActive();
    }

    @GetMapping("/post/{postId}/has-priority")
    @Operation(summary = "Check if a post has active priority subscription")
    public boolean hasActivePriority(@PathVariable Long postId) {
        return service.hasActivePrioritySubscription(postId);
    }

    @PostMapping("/expire-expired")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Expire all expired subscriptions (admin only)")
    public void expireExpiredSubscriptions() {
        service.expireExpiredSubscriptions();
    }
}
