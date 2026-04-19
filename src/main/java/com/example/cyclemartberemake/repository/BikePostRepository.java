package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.BikePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BikePostRepository extends JpaRepository<BikePost, Long> {
}