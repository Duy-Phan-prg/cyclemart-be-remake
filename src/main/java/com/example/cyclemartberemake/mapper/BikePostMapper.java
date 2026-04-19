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
    @Mapping(target = "postStatus", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "prioritySubscriptions", ignore = true)
    BikePost toEntity(BikePostRequest request);

    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "postStatus", target = "postStatusDisplay", qualifiedByName = "mapPostStatus")
    @Mapping(source = "rejectionReason", target = "rejectionReason")
    @Mapping(source = "images", target = "images", qualifiedByName = "mapImages")
    BikePostResponse toResponse(BikePost bikePost);

    List<BikePostResponse> toResponseList(List<BikePost> bikePosts);

    // Helper method to create BikePostRequest from parameters
    default BikePostRequest createRequest(String title, String description, Double price, BikeStatus status,
                                          City city, HCMDistrict district, BikeBrand brand, String model,
                                          Integer year, FrameMaterial frameMaterial, FrameSize frameSize,
                                          BrakeType brakeType, Groupset groupset, Integer mileage, Integer categoryId, Boolean allowNegotiation) {
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
        req.setAllowNegotiation(allowNegotiation);
        return req;
    }

    @org.mapstruct.Named("mapPostStatus")
    default String mapPostStatus(PostStatus status) {
        if (status == null) return null;
        return switch (status) {
            case PENDING -> "Chờ duyệt";
            case APPROVED -> "Đã duyệt";
            case REJECTED -> "Bị từ chối";
            case SOLD -> "Đã bán";
            case HIDDEN -> "Ẩn bài";
        };
    }

    @org.mapstruct.Named("mapImages")
    default List<String> mapImages(List<BikeImage> images) {
        if (images == null || images.isEmpty()) {
            return List.of();
        }
        return images.stream()
                .map(BikeImage::getUrl)
                .toList();
    }
}