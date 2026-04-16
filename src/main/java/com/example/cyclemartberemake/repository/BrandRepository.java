package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
    Optional<Brand> findByName(String name);

    List<Brand> findByIsActiveTrue();

    List<Brand> findAllByOrderByNameAsc();
}