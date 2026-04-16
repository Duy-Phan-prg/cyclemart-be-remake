package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.BikePostRequest;
import com.example.cyclemartberemake.dto.response.BikePostResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BikePostService {
    BikePostResponse create(BikePostRequest req, List<MultipartFile> images);

    List<BikePostResponse> getAll();
}
