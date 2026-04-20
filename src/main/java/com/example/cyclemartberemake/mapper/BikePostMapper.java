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
    @Mapping(source = "postStatus", target = "postStatus", qualifiedByName = "mapPostStatusEnum")
    @Mapping(source = "postStatus", target = "postStatusDisplay", qualifiedByName = "mapPostStatus")
    @Mapping(source = "status", target = "status", qualifiedByName = "mapBikeStatusEnum")
    @Mapping(source = "city", target = "city", qualifiedByName = "mapCityEnum")
    @Mapping(source = "district", target = "district", qualifiedByName = "mapDistrictEnum")
    @Mapping(source = "brand", target = "brand", qualifiedByName = "mapBrandEnum")
    @Mapping(source = "frameMaterial", target = "frameMaterial", qualifiedByName = "mapFrameMaterialEnum")
    @Mapping(source = "frameSize", target = "frameSize", qualifiedByName = "mapFrameSizeEnum")
    @Mapping(source = "brakeType", target = "brakeType", qualifiedByName = "mapBrakeTypeEnum")
    @Mapping(source = "groupset", target = "groupset", qualifiedByName = "mapGroupsetEnum")
    @Mapping(source = "images", target = "images", qualifiedByName = "mapImages")
    @Mapping(target = "activePriority", ignore = true) // Will be set manually in service
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

    @org.mapstruct.Named("mapPostStatusEnum")
    default String mapPostStatusEnum(PostStatus status) {
        return status != null ? status.name() : null;
    }

    @org.mapstruct.Named("mapBikeStatusEnum")
    default String mapBikeStatusEnum(BikeStatus status) {
        return status != null ? status.name() : null;
    }

    @org.mapstruct.Named("mapCityEnum")
    default String mapCityEnum(City city) {
        return city != null ? city.name() : null;
    }

    @org.mapstruct.Named("mapDistrictEnum")
    default String mapDistrictEnum(HCMDistrict district) {
        return district != null ? district.name() : null;
    }

    @org.mapstruct.Named("mapBrandEnum")
    default String mapBrandEnum(BikeBrand brand) {
        return brand != null ? brand.name() : null;
    }

    @org.mapstruct.Named("mapFrameMaterialEnum")
    default String mapFrameMaterialEnum(FrameMaterial material) {
        return material != null ? material.name() : null;
    }

    @org.mapstruct.Named("mapFrameSizeEnum")
    default String mapFrameSizeEnum(FrameSize size) {
        return size != null ? size.name() : null;
    }

    @org.mapstruct.Named("mapBrakeTypeEnum")
    default String mapBrakeTypeEnum(BrakeType type) {
        return type != null ? type.name() : null;
    }

    @org.mapstruct.Named("mapGroupsetEnum")
    default String mapGroupsetEnum(Groupset groupset) {
        return groupset != null ? groupset.name() : null;
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