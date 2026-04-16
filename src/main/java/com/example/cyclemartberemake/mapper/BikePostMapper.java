package com.example.cyclemartberemake.mapper;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
import com.example.cyclemartberemake.entity.*;
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

    // Helper method to create BikePostRequest from parameters
    default BikePostRequest createRequest(String title, String description, Double price, BikeStatus status,
                                          City city, HCMDistrict district, BikeBrand brand, String model,
                                          Integer year, FrameMaterial frameMaterial, FrameSize frameSize,
                                          BrakeType brakeType, Groupset groupset, Integer mileage, Integer categoryId) {
        BikePostRequest req = new BikePostRequest();
        req.setTitle(title);
        req.setDescription(description);
        req.setPrice(price);
        req.setStatus(status);
        req.setCity(city);
        req.setDistrict(district);
        req.setBrand(brand);
        req.setModel(model);
        req.setYear(year);
        req.setFrameMaterial(frameMaterial);
        req.setFrameSize(frameSize);
        req.setBrakeType(brakeType);
        req.setGroupset(groupset);
        req.setMileage(mileage);
        req.setCategoryId(categoryId);
        return req;
    }
}