package com.example.cyclemartberemake.mapper;

import com.example.cyclemartberemake.dto.response.SellerRatingResponse;
import com.example.cyclemartberemake.entity.SellerRating;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SellerRatingMapper {

    @Mapping(source = "seller.id", target = "sellerId")
    @Mapping(source = "seller.fullName", target = "sellerName")
    @Mapping(source = "seller.email", target = "sellerEmail")
    @Mapping(source = "buyer.id", target = "buyerId")
    @Mapping(source = "buyer.fullName", target = "buyerName")
    @Mapping(source = "buyer.email", target = "buyerEmail")
    SellerRatingResponse toResponse(SellerRating sellerRating);

    List<SellerRatingResponse> toResponseList(List<SellerRating> sellerRatings);
}
