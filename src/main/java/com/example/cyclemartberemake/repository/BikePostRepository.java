package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.BikePost;
import com.example.cyclemartberemake.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BikePostRepository extends JpaRepository<BikePost, Long> {

    List<BikePost> findByPostStatus(PostStatus status);

    Page<BikePost> findByPostStatus(PostStatus status, Pageable pageable);

    Page<BikePost> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT bp FROM BikePost bp WHERE " +
           "(:keyword IS NULL OR LOWER(bp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(bp.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:minPrice IS NULL OR bp.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR bp.price <= :maxPrice) AND " +
           "(:brand IS NULL OR bp.brand = :brand) AND " +
           "(:city IS NULL OR bp.city = :city) AND " +
           "bp.postStatus = 'APPROVED'")
    Page<BikePost> searchPosts(@Param("keyword") String keyword,
                              @Param("minPrice") Double minPrice,
                              @Param("maxPrice") Double maxPrice,
                              @Param("brand") String brand,
                              @Param("city") String city,
                              Pageable pageable);
}