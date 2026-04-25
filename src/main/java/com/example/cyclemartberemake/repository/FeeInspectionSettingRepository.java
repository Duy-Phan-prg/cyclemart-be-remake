package com.example.cyclemartberemake.repository;

import com.example.cyclemartberemake.entity.FeeInspectionSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeeInspectionSettingRepository extends JpaRepository<FeeInspectionSetting, String> {
}