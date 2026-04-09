package com.hunglevi.backend.dto.response;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    private long totalPackets;
    private long totalAlerts;
    private long openAlerts;
    private long resolvedAlerts;
    private Map<String, Long> alertsByType;
    private Map<String, Long> alertsBySeverity;
    private List<TimelinePoint> alertTimeline;
    private String systemStatus;
}

