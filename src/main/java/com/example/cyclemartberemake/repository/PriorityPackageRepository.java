package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.PriorityPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PriorityPackageRepository extends JpaRepository<PriorityPackage, Long> {
    List<PriorityPackage> findByIsActiveTrue();
    boolean existsByName(String name);
}

