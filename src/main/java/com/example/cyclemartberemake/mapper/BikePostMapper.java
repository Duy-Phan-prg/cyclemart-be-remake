package com.example.cyclemartberemake.mapper;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
import com.example.cyclemartberemake.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BikePostMapper {

    // ================= ENTITY =================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "postStatus", ignore = true)
    @Mapping(target = "approvedBy", ignore = true)
    @Mapping(target = "approvedAt", ignore = true)
    @Mapping(target = "rejectionReason", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "prioritySubscriptions", ignore = true)
    @Mapping(target = "isVerified", ignore = true)
    BikePost toEntity(BikePostRequest request);

    // ================= RESPONSE =================
    @Mapping(source = "category.name", target = "categoryName")

    // ENUM -> STRING
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

    // IMAGES
    @Mapping(source = "images", target = "images", qualifiedByName = "mapImages")

    // SELLER (🔥 FIX CHÍNH Ở ĐÂY)
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "sellerName")
    @Mapping(source = "user.email", target = "sellerEmail")

    @Mapping(target = "activePriority", ignore = true)

    BikePostResponse toResponse(BikePost bikePost);

    List<BikePostResponse> toResponseList(List<BikePost> bikePosts);

    // ================= CREATE REQUEST =================
    default BikePostRequest createRequest(
            String title, String description, Double price,
            BikeStatus status, City city, HCMDistrict district,
            BikeBrand brand, String model, Integer year,
            FrameMaterial frameMaterial, FrameSize frameSize,
            BrakeType brakeType, Groupset groupset, Integer mileage,
            Integer categoryId, Boolean allowNegotiation) {
        
        BikePostRequest request = new BikePostRequest();
        request.setTitle(title);
        request.setDescription(description);
        request.setPrice(price);
        request.setStatus(status);
        request.setCity(city);
        request.setDistrict(district);
        request.setBrand(brand);
        request.setModel(model);
        request.setYear(year);
        request.setFrameMaterial(frameMaterial);
        request.setFrameSize(frameSize);
        request.setBrakeType(brakeType);
        request.setGroupset(groupset);
        request.setMileage(mileage);
        request.setCategoryId(categoryId);
        request.setAllowNegotiation(allowNegotiation);
        return request;
    }

    // ================= ENUM MAPPERS =================

    @Named("mapPostStatus")
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

    @Named("mapPostStatusEnum")
    default String mapPostStatusEnum(PostStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("mapBikeStatusEnum")
    default String mapBikeStatusEnum(BikeStatus status) {
        return status != null ? status.name() : null;
    }

    @Named("mapCityEnum")
    default String mapCityEnum(City city) {
        return city != null ? city.name() : null;
    }

    @Named("mapDistrictEnum")
    default String mapDistrictEnum(HCMDistrict district) {
        return district != null ? district.name() : null;
    }

    @Named("mapBrandEnum")
    default String mapBrandEnum(BikeBrand brand) {
        return brand != null ? brand.name() : null;
    }

    @Named("mapFrameMaterialEnum")
    default String mapFrameMaterialEnum(FrameMaterial material) {
        return material != null ? material.name() : null;
    }

    @Named("mapFrameSizeEnum")
    default String mapFrameSizeEnum(FrameSize size) {
        return size != null ? size.name() : null;
    }

    @Named("mapBrakeTypeEnum")
    default String mapBrakeTypeEnum(BrakeType type) {
        return type != null ? type.name() : null;
    }

    @Named("mapGroupsetEnum")
    default String mapGroupsetEnum(Groupset groupset) {
        return groupset != null ? groupset.name() : null;
    }

    // ================= IMAGE =================
    @Named("mapImages")
    default List<String> mapImages(List<BikeImage> images) {
        if (images == null || images.isEmpty()) return List.of();

        return images.stream()
                .map(BikeImage::getUrl)
                .toList();
    }
}