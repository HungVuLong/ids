package com.hunglevi.backend.service;

import com.hunglevi.backend.dto.AlertResponse;
import com.hunglevi.backend.dto.AlertStatusUpdateRequest;
import com.hunglevi.backend.dto.ml.MLResponse;
import com.hunglevi.backend.entity.Alert;
import com.hunglevi.backend.entity.Packet;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface AlertService {

    /**
     * Create a new alert for a packet
     */
    AlertResponse createAlert(Long packetId, String alertType, double confidence);

    /**
     * Create a new alert for a packet and return the entity for notification.
     */
    Alert createAlert(Packet packet, MLResponse mlResponse);

    /**
     * Get paginated list of alerts
     */
    Page<AlertResponse> getAlerts(int page, int size, String status, String attackType);

    /**
     * Update alert status (resolve or ignore)
     */
    AlertResponse updateAlertStatus(Long id, AlertStatusUpdateRequest request, String username);

    /**
     * Update alert status with resolved by username
     */
    AlertResponse updateStatus(Long id, String status, String resolvedBy);

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

    /**
     * Get a specific alert by ID
     */
    AlertResponse getAlertById(Long id);

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
