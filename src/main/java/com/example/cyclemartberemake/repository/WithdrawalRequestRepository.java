package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.WithdrawalRequest;
import com.example.cyclemartberemake.entity.WithdrawalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    Page<WithdrawalRequest> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<WithdrawalRequest> findByStatusOrderByCreatedAtDesc(WithdrawalStatus status, Pageable pageable);
    Page<WithdrawalRequest> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
