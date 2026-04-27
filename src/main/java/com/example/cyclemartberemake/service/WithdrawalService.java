package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.WithdrawalRequestDTO;
import com.example.cyclemartberemake.dto.response.WithdrawalResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WithdrawalService {
    WithdrawalResponse createRequest(Long userId, WithdrawalRequestDTO dto);
    Page<WithdrawalResponse> getMyRequests(Long userId, Pageable pageable);
    Page<WithdrawalResponse> getAllForAdmin(Pageable pageable);
    WithdrawalResponse complete(Long requestId, Long adminId, String note);
    WithdrawalResponse reject(Long requestId, Long adminId, String note);
}
