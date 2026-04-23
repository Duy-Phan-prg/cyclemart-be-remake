// File: cyclemartberemake/controller/InspectionCriterionController.java
package com.example.cyclemartberemake.controller;

import com.example.cyclemartberemake.entity.InspectionCriterion;
import com.example.cyclemartberemake.repository.InspectionCriterionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inspection-criteria")
@RequiredArgsConstructor
public class InspectionCriterionController {

    private final InspectionCriterionRepository repository;

    // Lấy tất cả (Cho Admin quản lý)
    @GetMapping
    public List<InspectionCriterion> getAll() {
        return repository.findAll();
    }

    // Lấy danh sách đang hoạt động (Cho Inspector làm việc)
    @GetMapping("/active")
    public List<InspectionCriterion> getActive() {
        return repository.findByIsActiveTrue();
    }

    @PostMapping
    public InspectionCriterion create(@RequestBody InspectionCriterion criterion) {
        return repository.save(criterion);
    }

    @PutMapping("/{id}")
    public InspectionCriterion update(@PathVariable Long id, @RequestBody InspectionCriterion data) {
        InspectionCriterion existing = repository.findById(id).orElseThrow();
        existing.setName(data.getName());
        existing.setDescription(data.getDescription());
        existing.setIsActive(data.getIsActive());
        return repository.save(existing);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}