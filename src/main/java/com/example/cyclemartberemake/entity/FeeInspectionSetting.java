package com.example.cyclemartberemake.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fee_inspection_settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeInspectionSetting {
    @Id
    private String settingKey;
    private String settingValue;
}