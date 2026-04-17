package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.PostPrioritySubscriptionRequest;
import com.example.cyclemartberemake.dto.response.PostPrioritySubscriptionResponse;

import java.util.List;
import java.util.Map;

public interface PostPrioritySubscriptionService {

    PostPrioritySubscriptionResponse subscribe(PostPrioritySubscriptionRequest request);

    Map<String, Object> unsubscribe(Long id);

    Map<String, Object> unsubscribeByPostId(Long postId);

    PostPrioritySubscriptionResponse getById(Long id);

    List<PostPrioritySubscriptionResponse> getByPostId(Long postId);

    List<PostPrioritySubscriptionResponse> getAllActive();

    boolean hasActivePrioritySubscription(Long postId);

    void expireExpiredSubscriptions();
}
