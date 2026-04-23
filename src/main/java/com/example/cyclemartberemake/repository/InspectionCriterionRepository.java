package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.InspectionCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionCriterionRepository extends JpaRepository<InspectionCriterion, Long> {
    List<InspectionCriterion> findByIsActiveTrue();
}