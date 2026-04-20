package com.hunglevi.backend.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private String title;
    private String generatedBy;
    private LocalDate fromDate;
    private LocalDate toDate;
    private long totalPackets;
    private long totalAlerts;
    private Map<String, Long> alertsByType;
    private LocalDateTime generatedAt;
}

