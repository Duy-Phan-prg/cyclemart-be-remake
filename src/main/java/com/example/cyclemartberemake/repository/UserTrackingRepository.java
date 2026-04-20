package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.UserTracking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserTrackingRepository extends JpaRepository<UserTracking, Integer> {

    // Lấy danh sách log theo ID người dùng
    Page<UserTracking> findByUserId(int userId, Pageable pageable);
}