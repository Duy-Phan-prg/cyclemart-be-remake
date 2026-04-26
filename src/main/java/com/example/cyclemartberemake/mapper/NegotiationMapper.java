package com.example.cyclemartberemake.mapper;

import com.example.cyclemartberemake.dto.response.NegotiationResponseDTO;
import com.example.cyclemartberemake.entity.Negotiation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NegotiationMapper {

    @Mapping(source = "bikePost.id", target = "bikePostId")
    @Mapping(source = "buyer.id", target = "buyerId")
    @Mapping(target = "paymentUrl", ignore = true)
    @Mapping(target = "paymentOrderId", ignore = true)
    NegotiationResponseDTO toResponse(Negotiation negotiation);

    List<NegotiationResponseDTO> toResponseList(List<Negotiation> negotiations);
}
