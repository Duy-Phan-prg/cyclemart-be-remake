package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.Categories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Categories, Integer> {

    // Kiểm tra tên category đã tồn tại
    boolean existsByName(String name);
    
    // Kiểm tra tên category đã tồn tại (trừ ID hiện tại)
    boolean existsByNameAndIdNot(String name, Integer id);
    
    // Kiểm tra có danh mục con không
    boolean existsByParentId(Integer parentId);
    
    // Đếm số bike posts theo category
    @Query("SELECT COUNT(bp) FROM BikePost bp WHERE bp.category.id = :categoryId")
    long countBikePostsByCategory(@Param("categoryId") Integer categoryId);
    
    // Tìm theo parent
    List<Categories> findByParent(Categories parent);

    // Tìm theo parent ID
    @Query("SELECT c FROM Categories c WHERE c.parent.id = :parentId")
    List<Categories> findByParentId(@Param("parentId") Integer parentId);
    
    // Tìm theo active status
    List<Categories> findByIsActive(Boolean isActive);
    
    // Tìm active categories theo parent
    @Query("SELECT c FROM Categories c WHERE c.parent.id = :parentId AND c.isActive = true")
    List<Categories> findActiveByParentId(@Param("parentId") Integer parentId);

    // Lấy tất cả category con (parent khác null)
    @Query("SELECT c FROM Categories c WHERE c.parent IS NOT NULL")
    List<Categories> findAllChildCategories();
}