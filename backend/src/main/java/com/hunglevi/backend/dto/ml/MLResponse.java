package com.hunglevi.backend.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLResponse {
    private String label;
    private String attackType;
    private Double confidence;
    private Long predictionTimeMs;

    public boolean isAttack() {
        return "attack".equals(label);
    }

    public boolean isHighConfidence() {
        return confidence != null && confidence >= 0.7;
    }
}
