package com.example.cyclemartberemake.mapper;

import com.example.cyclemartberemake.dto.response.NegotiationResponseDTO;
import com.example.cyclemartberemake.entity.Negotiation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NegotiationMapper {

    @Mapping(source = "bikePost.id", target = "bikePostId")
    NegotiationResponseDTO toResponse(Negotiation negotiation);

    List<NegotiationResponseDTO> toResponseList(List<Negotiation> negotiations);
}