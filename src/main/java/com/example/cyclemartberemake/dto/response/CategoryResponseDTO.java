package com.example.cyclemartberemake.dto.response;

import lombok.Builder;
import lombok.Data;

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
}
