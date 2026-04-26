package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.NegotiationRequestDTO;
import com.example.cyclemartberemake.dto.response.NegotiationResponseDTO;

import java.util.List;

public interface NegotiationService {
    NegotiationResponseDTO createNegotiation(Long buyerId, NegotiationRequestDTO request);

    NegotiationResponseDTO acceptNegotiation(Long negotiationId, Long sellerId) throws Exception;

    NegotiationResponseDTO rejectNegotiation(Long negotiationId, Long sellerId);

    NegotiationResponseDTO counterNegotiation(Long negotiationId, Long sellerId, Double counterPrice);

    List<NegotiationResponseDTO> getByPostId(Long postId, Long sellerId);

    List<NegotiationResponseDTO> getByBuyer(Long buyerId);
}