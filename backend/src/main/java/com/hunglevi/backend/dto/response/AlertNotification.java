package com.hunglevi.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertNotification {
    private Long alertId;
    private String attackType;
    private String severity;
    private String message;
    private String srcIp;
    private String dstIp;
    private Double confidence;
    private String detectedAt;
    private String status;
}

