package com.hunglevi.backend.controller;

import com.hunglevi.backend.dto.AlertResponse;
import com.hunglevi.backend.dto.AlertStatusUpdateRequest;
import com.hunglevi.backend.service.AlertService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    /**
     * GET /api/alerts - Get all alerts with optional filtering
     * Query params: page, size, status, alertType
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String alertType) {

        log.info("GET /api/alerts - page: {}, size: {}, status: {}, alertType: {}", page, size, status, alertType);

        Page<AlertResponse> alerts = alertService.getAlerts(page, size, status, alertType);

        Map<String, Object> response = new HashMap<>();
        response.put("content", alerts.getContent());
        response.put("totalElements", alerts.getTotalElements());
        response.put("totalPages", alerts.getTotalPages());
        response.put("currentPage", page);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/alerts/{id} - Get alert by ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AlertResponse> getAlertById(@PathVariable Long id) {
        log.info("GET /api/alerts/{} - Fetching alert", id);

        AlertResponse alert = alertService.getAlertById(id);
        return ResponseEntity.ok(alert);
    }

    /**
     * PATCH /api/alerts/{id}/status - Update alert status
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AlertResponse> updateAlertStatus(
            @PathVariable Long id,
            @Valid @RequestBody AlertStatusUpdateRequest request,
            Authentication authentication) {

        log.info("PATCH /api/alerts/{}/status - Updating status to {}", id, request.getStatus());

        String username = authentication.getName();
        AlertResponse updatedAlert = alertService.updateAlertStatus(id, request, username);

        return ResponseEntity.ok(updatedAlert);
    }

    /**
     * DELETE /api/alerts/{id} - Delete alert
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAlert(@PathVariable Long id) {
        log.info("DELETE /api/alerts/{} - Deleting alert", id);

        alertService.deleteAlert(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/alerts/stats - Get alert statistics
     */
    @GetMapping("/stats/overview")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<AlertService.AlertStatsDto> getAlertStats() {
        log.info("GET /api/alerts/stats/overview - Fetching alert statistics");

        AlertService.AlertStatsDto stats = alertService.getAlertStats();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/alerts/count/{status} - Count alerts by status
     */
    @GetMapping("/count/{status}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> countByStatus(@PathVariable String status) {
        log.info("GET /api/alerts/count/{} - Counting alerts", status);

        long count = alertService.countByStatus(status);

        Map<String, Object> response = new HashMap<>();
        response.put("status", status);
        response.put("count", count);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/alerts/timeline - Get timeline data since a date
     * Query param: days (number of days back, default 7)
     */
    @GetMapping("/timeline")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getTimeline(
            @RequestParam(defaultValue = "7") int days) {

        log.info("GET /api/alerts/timeline - Fetching timeline for last {} days", days);

        LocalDateTime from = LocalDateTime.now().minusDays(days);
        List<Object[]> timeline = alertService.getTimelineSince(from);

        Map<String, Object> response = new HashMap<>();
        response.put("period", days + " days");
        response.put("data", timeline);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/alerts/by-type - Get alert count grouped by type
     */
    @GetMapping("/by-type")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> getAlertsByType() {
        log.info("GET /api/alerts/by-type - Fetching alerts grouped by type");

        List<Object[]> data = alertService.countGroupByAlertType();

        Map<String, Object> response = new HashMap<>();
        response.put("data", data);

        return ResponseEntity.ok(response);
    }
}

