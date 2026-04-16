package com.example.cyclemartberemake.mapper;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
import com.example.cyclemartberemake.entity.BikePost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BikePostMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BikePost toEntity(BikePostRequest request);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(target = "images", ignore = true) // Will be handled separately
    BikePostResponse toResponse(BikePost bikePost);

    List<BikePostResponse> toResponseList(List<BikePost> bikePosts);
}