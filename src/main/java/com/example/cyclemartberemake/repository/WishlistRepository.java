package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.WishlistItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    Optional<WishlistItem> findByUserIdAndPostId(Long userId, Long postId);

    Page<WishlistItem> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
