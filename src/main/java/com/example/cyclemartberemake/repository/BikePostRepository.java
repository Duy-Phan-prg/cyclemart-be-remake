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

    //  Lấy danh sách có sắp xếp theo Gói ưu tiên (PLATINUM > GOLD > SILVER > NO_PKG)
    @Query("SELECT bp FROM BikePost bp " +
            "LEFT JOIN bp.prioritySubscriptions ps ON ps.isActive = true AND ps.endDate >= CURRENT_TIMESTAMP " +
            "LEFT JOIN ps.priorityPackage pkg " +
            "WHERE bp.postStatus = :status " +
            "ORDER BY CASE " +
            "  WHEN pkg.priorityLevel = com.example.cyclemartberemake.entity.PriorityLevel.PLATINUM THEN 3 " +
            "  WHEN pkg.priorityLevel = com.example.cyclemartberemake.entity.PriorityLevel.GOLD THEN 2 " +
            "  WHEN pkg.priorityLevel = com.example.cyclemartberemake.entity.PriorityLevel.SILVER THEN 1 " +
            "  ELSE 0 END DESC")
    Page<BikePost> findApprovedPostsWithPriority(@Param("status") PostStatus status, Pageable pageable);

    Page<BikePost> findByUserId(Long userId, Pageable pageable);

    //  Tìm kiếm kết hợp sắp xếp Gói ưu tiên
    @Query("SELECT bp FROM BikePost bp " +
            "LEFT JOIN bp.prioritySubscriptions ps ON ps.isActive = true AND ps.endDate >= CURRENT_TIMESTAMP " +
            "LEFT JOIN ps.priorityPackage pkg " +
            "WHERE (:keyword IS NULL OR LOWER(bp.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(bp.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:minPrice IS NULL OR bp.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR bp.price <= :maxPrice) AND " +
            "(:brand IS NULL OR bp.brand = :brand) AND " +
            "(:city IS NULL OR bp.city = :city) AND " +
            "(:categoryId IS NULL OR bp.category.id = :categoryId) AND " +
            "bp.postStatus = 'APPROVED' " +
            "ORDER BY CASE " +
            "  WHEN pkg.priorityLevel = com.example.cyclemartberemake.entity.PriorityLevel.PLATINUM THEN 3 " +
            "  WHEN pkg.priorityLevel = com.example.cyclemartberemake.entity.PriorityLevel.GOLD THEN 2 " +
            "  WHEN pkg.priorityLevel = com.example.cyclemartberemake.entity.PriorityLevel.SILVER THEN 1 " +
            "  ELSE 0 END DESC")
    Page<BikePost> searchPostsWithPriority(@Param("keyword") String keyword,
                                           @Param("minPrice") Double minPrice,
                                           @Param("maxPrice") Double maxPrice,
                                           @Param("brand") String brand,
                                           @Param("city") String city,
                                           @Param("categoryId") Integer categoryId,
                                           Pageable pageable);
}