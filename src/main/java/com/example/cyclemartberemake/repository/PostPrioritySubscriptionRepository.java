package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.PostPrioritySubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostPrioritySubscriptionRepository extends JpaRepository<PostPrioritySubscription, Long> {

    List<PostPrioritySubscription> findByPostIdAndIsActiveTrue(Long postId);

    List<PostPrioritySubscription> findByIsActiveTrueOrderByPriorityPackage_PriorityLevelDesc();

    @Query("SELECT pps FROM PostPrioritySubscription pps " +
           "WHERE pps.post.id = :postId AND pps.isActive = true " +
           "AND pps.endDate > CURRENT_TIMESTAMP")
    List<PostPrioritySubscription> findActiveSubscriptionsByPostId(@Param("postId") Long postId);

    @Query("SELECT pps FROM PostPrioritySubscription pps " +
           "WHERE pps.isActive = true AND pps.endDate > CURRENT_TIMESTAMP " +
           "ORDER BY pps.priorityPackage.priorityLevel DESC")
    List<PostPrioritySubscription> findActiveSubscriptionsSorted();

    Optional<PostPrioritySubscription> findByPostIdAndPriorityPackageId(Long postId, Long packageId);

    @Query("SELECT COUNT(pps) > 0 FROM PostPrioritySubscription pps " +
           "WHERE pps.post.id = :postId AND pps.isActive = true " +
           "AND pps.endDate > CURRENT_TIMESTAMP")
    boolean hasActivePrioritySubscription(@Param("postId") Long postId);
}
