package com.example.cyclemartberemake.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder

public class CategoryResponseDTO {
    private Integer id;
    private String name;
    private String description;
    private String icon;
    private Integer displayOrder;
    private Integer parentId;
    private String parentName;
    private Boolean isActive;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<CategoryResponseDTO> children;
}
