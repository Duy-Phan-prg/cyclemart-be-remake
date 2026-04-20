package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.Inspection;
import com.example.cyclemartberemake.entity.InspectionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {
    Page<Inspection> findBySellerId(int sellerId, Pageable pageable);
    Page<Inspection> findByInspectorId(int inspectorId, Pageable pageable);
    Page<Inspection> findByStatus(InspectionStatus status, Pageable pageable);
    boolean existsByBikePostIdAndStatusIn(Long postId, java.util.List<InspectionStatus> statuses);
    // Lấy tất cả lịch đã gán cho Inspector (trừ những cái đã hủy)
    List<Inspection> findByInspectorIdAndStatusIn(Integer inspectorId, List<InspectionStatus> statuses);
}