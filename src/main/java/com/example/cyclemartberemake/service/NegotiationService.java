package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.NegotiationRequestDTO;
import com.example.cyclemartberemake.dto.response.NegotiationResponseDTO;

public interface NegotiationService {
    NegotiationResponseDTO createNegotiation(Long buyerId, NegotiationRequestDTO request);
}