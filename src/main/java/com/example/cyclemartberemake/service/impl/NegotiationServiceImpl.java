package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.NegotiationRequestDTO;
import com.example.cyclemartberemake.dto.response.NegotiationResponseDTO;
import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.Negotiation;
import com.example.cyclemartberemake.entity.NegotiationStatus;
import com.example.cyclemartberemake.entity.Users;
import com.example.cyclemartberemake.mapper.NegotiationMapper;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.NegotiationRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.NegotiationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NegotiationServiceImpl implements NegotiationService {

    private final NegotiationRepository negotiationRepository;
    private final BikePostRepository bikePostRepository;
    private final UserRepository userRepository;
    private final NegotiationMapper negotiationMapper;

    @Override
    public NegotiationResponseDTO createNegotiation(Long buyerId, NegotiationRequestDTO request) {
        BikePost post = bikePostRepository.findById(request.getBikePostId())
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        Users buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        Negotiation negotiation = Negotiation.builder()
                .buyer(buyer)
                .bikePost(post)
                .offeredPrice(request.getOfferedPrice())
                .status(NegotiationStatus.PENDING)
                .build();

        Negotiation saved = negotiationRepository.save(negotiation);
        return negotiationMapper.toResponse(saved);
    }
}