package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BikePostService {
    BikePostResponse create(BikePostRequest req, List<MultipartFile> images);

    Page<BikePostResponse> getAll(Pageable pageable);
    
    BikePostResponse getById(Long id);
    
    BikePostResponse update(Long id, BikePostRequest req, List<MultipartFile> images);
    
    void delete(Long id);
    
    Page<BikePostResponse> getMyPosts(Pageable pageable);

    Page<BikePostResponse> getPostsByUserId(Long userId, Pageable pageable);

    Page<BikePostResponse> search(String keyword, Double minPrice, Double maxPrice, 
                                 String brand, String city, Integer categoryId, Pageable pageable);
    
    // Admin methods
    void approve(Long id);
    
    void reject(Long id, String reason);
    
    Page<BikePostResponse> getAllForAdmin(Pageable pageable);
}
