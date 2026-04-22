package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.Report;
import com.example.cyclemartberemake.entity.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    List<Report> findByStatus(ReportStatus status);
    
    List<Report> findByReporterId(Long reporterId);
    
    List<Report> findByReportedUserId(Long reportedUserId);
    
    // Thống kê
    long countByStatus(ReportStatus status);
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'PENDING'")
    long countPendingReports();
    
    @Query("SELECT COUNT(r) FROM Report r WHERE r.status = 'RESOLVED'")
    long countResolvedReports();
}