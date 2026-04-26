package com.example.cyclemartberemake.service.impl;

import com.example.cyclemartberemake.dto.request.NegotiationRequestDTO;
import com.example.cyclemartberemake.dto.response.CreatePaymentResponse;
import com.example.cyclemartberemake.dto.response.NegotiationResponseDTO;
import com.example.cyclemartberemake.entity.*;
import com.example.cyclemartberemake.mapper.NegotiationMapper;
import com.example.cyclemartberemake.repository.BikePostRepository;
import com.example.cyclemartberemake.repository.NegotiationRepository;
import com.example.cyclemartberemake.repository.UserRepository;
import com.example.cyclemartberemake.service.NegotiationService;
import com.example.cyclemartberemake.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NegotiationServiceImpl implements NegotiationService {

    private final NegotiationRepository negotiationRepository;
    private final BikePostRepository bikePostRepository;
    private final UserRepository userRepository;
    private final NegotiationMapper negotiationMapper;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public NegotiationResponseDTO createNegotiation(Long buyerId, NegotiationRequestDTO request) {
        BikePost post = bikePostRepository.findById(request.getBikePostId())
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));

        if (post.getPostStatus() != PostStatus.APPROVED) {
            throw new RuntimeException("Chỉ có thể đặt giá cho bài đăng đã được duyệt");
        }

        Users buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại"));

        if (post.getUser() != null && post.getUser().getId().equals(buyerId)) {
            throw new RuntimeException("Bạn không thể đặt giá cho bài đăng của chính mình");
        }

        Negotiation negotiation = Negotiation.builder()
                .buyer(buyer)
                .bikePost(post)
                .offeredPrice(request.getOfferedPrice())
                .status(NegotiationStatus.PENDING)
                .build();

        Negotiation saved = negotiationRepository.save(negotiation);
        return negotiationMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public NegotiationResponseDTO acceptNegotiation(Long negotiationId, Long sellerId) throws Exception {
        Negotiation negotiation = findAndValidateSeller(negotiationId, sellerId);

        if (negotiation.getStatus() != NegotiationStatus.PENDING && negotiation.getStatus() != NegotiationStatus.COUNTERED) {
            throw new RuntimeException("Chỉ có thể chấp nhận offer đang ở trạng thái PENDING hoặc COUNTERED");
        }

        BikePost post = negotiation.getBikePost();
        Double finalPrice = negotiation.getStatus() == NegotiationStatus.COUNTERED && negotiation.getCounterPrice() != null
                ? negotiation.getCounterPrice()
                : negotiation.getOfferedPrice();

        // Tạo payment cho buyer
        Long buyerId = negotiation.getBuyer().getId();
        String description = "Mua xe: " + post.getTitle();
        CreatePaymentResponse paymentResponse = paymentService.createOrderPaymentForNegotiation(
                buyerId, post.getId(), finalPrice.longValue(), description);

        // Cập nhật trạng thái negotiation
        negotiation.setStatus(NegotiationStatus.ACCEPTED);
        negotiationRepository.save(negotiation);

        // Hủy các offer khác đang chờ của cùng bài đăng
        List<Negotiation> otherOffers = negotiationRepository.findByBikePostId(post.getId());
        otherOffers.stream()
                .filter(n -> !n.getId().equals(negotiationId) && n.getStatus() == NegotiationStatus.PENDING)
                .forEach(n -> {
                    n.setStatus(NegotiationStatus.REJECTED);
                    negotiationRepository.save(n);
                });

        NegotiationResponseDTO response = negotiationMapper.toResponse(negotiation);
        response.setPaymentUrl(paymentResponse.getPaymentUrl());
        response.setPaymentOrderId(paymentResponse.getOrderId());
        return response;
    }

    @Override
    @Transactional
    public NegotiationResponseDTO rejectNegotiation(Long negotiationId, Long sellerId) {
        Negotiation negotiation = findAndValidateSeller(negotiationId, sellerId);

        if (negotiation.getStatus() != NegotiationStatus.PENDING && negotiation.getStatus() != NegotiationStatus.COUNTERED) {
            throw new RuntimeException("Chỉ có thể từ chối offer đang ở trạng thái PENDING hoặc COUNTERED");
        }

        negotiation.setStatus(NegotiationStatus.REJECTED);
        negotiationRepository.save(negotiation);
        return negotiationMapper.toResponse(negotiation);
    }

    @Override
    @Transactional
    public NegotiationResponseDTO counterNegotiation(Long negotiationId, Long sellerId, Double counterPrice) {
        Negotiation negotiation = findAndValidateSeller(negotiationId, sellerId);

        if (negotiation.getStatus() != NegotiationStatus.PENDING) {
            throw new RuntimeException("Chỉ có thể counter offer đang ở trạng thái PENDING");
        }
        if (counterPrice == null || counterPrice <= 0) {
            throw new RuntimeException("Giá counter không hợp lệ");
        }

        negotiation.setCounterPrice(counterPrice);
        negotiation.setStatus(NegotiationStatus.COUNTERED);
        negotiationRepository.save(negotiation);
        return negotiationMapper.toResponse(negotiation);
    }

    @Override
    public List<NegotiationResponseDTO> getByPostId(Long postId, Long sellerId) {
        BikePost post = bikePostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Bài đăng không tồn tại"));
        if (post.getUser() == null || !post.getUser().getId().equals(sellerId)) {
            throw new RuntimeException("Bạn không có quyền xem offer của bài đăng này");
        }
        return negotiationMapper.toResponseList(negotiationRepository.findByBikePostId(postId));
    }

    @Override
    public List<NegotiationResponseDTO> getByBuyer(Long buyerId) {
        return negotiationMapper.toResponseList(negotiationRepository.findByBuyerId(buyerId));
    }

    private Negotiation findAndValidateSeller(Long negotiationId, Long sellerId) {
        Negotiation negotiation = negotiationRepository.findById(negotiationId)
                .orElseThrow(() -> new RuntimeException("Offer không tồn tại"));
        BikePost post = negotiation.getBikePost();
        if (post.getUser() == null || !post.getUser().getId().equals(sellerId)) {
            throw new RuntimeException("Bạn không có quyền thực hiện hành động này");
        }
        return negotiation;
    }
}
