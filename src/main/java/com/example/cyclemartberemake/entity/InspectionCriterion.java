// File: cyclemartberemake/entity/InspectionCriterion.java
package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inspection_criteria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InspectionCriterion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // VD: "Khung xe (Nứt, gãy, sơn lại)"

    private String description;

    @Column(nullable = false)
    private Boolean isActive = true; // Trạng thái để ẩn/hiện tiêu chí
}