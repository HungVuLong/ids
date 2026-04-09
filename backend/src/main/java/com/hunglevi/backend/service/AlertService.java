package com.hunglevi.backend.service;

import com.hunglevi.backend.dto.AlertResponse;
import com.hunglevi.backend.dto.AlertStatusUpdateRequest;
import com.hunglevi.backend.entity.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertService {

    /**
     * Create a new alert for a packet
     */
    AlertResponse createAlert(Long packetId, String alertType, double confidence);

    /**
     * Get paginated list of alerts
     */
    Page<AlertResponse> getAlerts(int page, int size, String status, String alertType);

    /**
     * Get a specific alert by ID
     */
    AlertResponse getAlertById(Long id);

    /**
     * Update alert status (resolve or ignore)
     */
    AlertResponse updateAlertStatus(Long id, AlertStatusUpdateRequest request, String username);

    /**
     * Delete an alert
     */
    void deleteAlert(Long id);

    /**
     * Count alerts by status
     */
    long countByStatus(String status);

    /**
     * Get alert count grouped by type
     */
    List<Object[]> countGroupByAlertType();

    /**
     * Get timeline data of alerts since a given date
     */
    List<Object[]> getTimelineSince(LocalDateTime from);

    /**
     * Get statistics for dashboard
     */
    AlertStatsDto getAlertStats();

    @lombok.Data
    @lombok.Builder
    class AlertStatsDto {
        private long openCount;
        private long resolvedCount;
        private long ignoredCount;
        private long totalCount;
        private List<Object[]> alertsByType;
    }
}

