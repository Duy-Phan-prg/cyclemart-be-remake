package com.example.cyclemartberemake.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Categories {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String description;
    private String icon;

    private Integer displayOrder;
    private Boolean isActive = true;

    @ManyToOne
    @JoinColumn(name = "parent_id")
    private Categories parent;

    @OneToMany(mappedBy = "parent")
    private List<Categories> children;

    @JsonIgnore
    @OneToMany(mappedBy = "category")
    private List<BikePost> posts;
}