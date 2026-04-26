package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.DisputeRequest;
import com.example.cyclemartberemake.dto.response.DisputeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DisputeService {

    DisputeResponse openDispute(Long buyerId, DisputeRequest request);

    DisputeResponse sellerApprove(Long disputeId, Long sellerId);

    DisputeResponse sellerReject(Long disputeId, Long sellerId);

    DisputeResponse adminResolve(Long disputeId, Long adminId, String resolution, String resolutionNote);

    Page<DisputeResponse> getMyDisputesAsBuyer(Long buyerId, Pageable pageable);

    Page<DisputeResponse> getMyDisputesAsSeller(Long sellerId, Pageable pageable);

    Page<DisputeResponse> getAllForAdmin(Pageable pageable);
}
