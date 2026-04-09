package com.hunglevi.backend.service.impl;

import com.hunglevi.backend.dto.response.DashboardStatsResponse;
import com.hunglevi.backend.dto.response.TimelinePoint;
import com.hunglevi.backend.repository.AlertRepository;
import com.hunglevi.backend.repository.PacketRepository;
import com.hunglevi.backend.service.DashboardService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {

    private static final String OPEN_STATUS = "OPEN";
    private static final String RESOLVED_STATUS = "RESOLVED";

    private final AlertRepository alertRepository;
    private final PacketRepository packetRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        LocalDateTime timelineFrom = LocalDate.now().minusDays(6).atStartOfDay();

        long totalPackets = packetRepository.count();
        long totalAlerts = alertRepository.count();
        long openAlerts = alertRepository.countByStatus(OPEN_STATUS);
        long resolvedAlerts = alertRepository.countByStatus(RESOLVED_STATUS);

        return DashboardStatsResponse.builder()
            .totalPackets(totalPackets)
            .totalAlerts(totalAlerts)
            .openAlerts(openAlerts)
            .resolvedAlerts(resolvedAlerts)
            .alertsByType(toMap(alertRepository.countGroupByAttackType()))
            .alertsBySeverity(toMap(alertRepository.countGroupBySeverity()))
            .alertTimeline(toTimeline(alertRepository.getTimelineSince(timelineFrom)))
            .systemStatus(openAlerts > 10 ? "UNDER_ATTACK" : "NORMAL")
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStatsByDateRange(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        long totalPackets = packetRepository.countByCapturedAtBetween(fromDateTime, toDateTime);
        long totalAlerts = alertRepository.countByTimestampBetween(fromDateTime, toDateTime);
        long openAlerts = alertRepository.countByStatusAndTimestampBetween(OPEN_STATUS, fromDateTime, toDateTime);
        long resolvedAlerts = alertRepository.countByStatusAndTimestampBetween(RESOLVED_STATUS, fromDateTime, toDateTime);

        return DashboardStatsResponse.builder()
            .totalPackets(totalPackets)
            .totalAlerts(totalAlerts)
            .openAlerts(openAlerts)
            .resolvedAlerts(resolvedAlerts)
            .alertsByType(toMap(alertRepository.countGroupByAttackTypeBetween(fromDateTime, toDateTime)))
            .alertsBySeverity(toMap(alertRepository.countGroupBySeverityBetween(fromDateTime, toDateTime)))
            .alertTimeline(toTimeline(alertRepository.getTimelineBetween(fromDateTime, toDateTime)))
            .systemStatus(openAlerts > 10 ? "UNDER_ATTACK" : "NORMAL")
            .build();
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();

        for (Object[] row : rows) {
            String key = row[0] != null ? row[0].toString() : "UNKNOWN";
            long value = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            result.put(key, value);
        }

        return result;
    }

    private List<TimelinePoint> toTimeline(List<Object[]> rows) {
        return rows.stream()
            .map(row -> TimelinePoint.builder()
                .date(row[0] != null ? row[0].toString() : "")
                .count(row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L)
                .build())
            .toList();
    }
}

