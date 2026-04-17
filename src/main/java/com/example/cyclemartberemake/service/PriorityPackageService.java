package com.example.cyclemartberemake.service;

import com.example.cyclemartberemake.dto.request.CreatePriorityPackageRequest;
import com.example.cyclemartberemake.dto.request.PriorityPackageRequest;
import com.example.cyclemartberemake.dto.response.PriorityPackageResponse;

import java.util.List;

public interface PriorityPackageService {

    PriorityPackageResponse create(CreatePriorityPackageRequest request);

    PriorityPackageResponse update(Long id, PriorityPackageRequest request);

    void delete(Long id);

    PriorityPackageResponse getById(Long id);

    List<PriorityPackageResponse> getAll();

    List<PriorityPackageResponse> getActivePackages();
}
