package com.example.cyclemartberemake.mapper;

import com.example.cyclemartberemake.dto.request.CategoryRequestDTO;
import com.example.cyclemartberemake.dto.response.CategoryResponseDTO;
import com.example.cyclemartberemake.entity.Categories;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "id", ignore = true)
    Categories toEntity(CategoryRequestDTO request);

    @Mapping(source = "parent.id", target = "parentId")
    @Mapping(source = "parent.name", target = "parentName")
    CategoryResponseDTO toResponse(Categories category);

    List<CategoryResponseDTO> toResponseList(List<Categories> categories);

    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "id", ignore = true)
    void updateEntity(CategoryRequestDTO request, @MappingTarget Categories category);
}