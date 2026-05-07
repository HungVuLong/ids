package com.hunglevi.backend.service.impl;

import com.hunglevi.backend.dto.AlertResponse;
import com.hunglevi.backend.dto.AlertStatusUpdateRequest;
import com.hunglevi.backend.dto.ml.MLResponse;
import com.hunglevi.backend.entity.Alert;
import com.hunglevi.backend.entity.Packet;
import com.hunglevi.backend.entity.User;
import com.hunglevi.backend.repository.AlertRepository;
import com.hunglevi.backend.repository.PacketRepository;
import com.hunglevi.backend.repository.UserRepository;
import com.hunglevi.backend.service.AlertService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final PacketRepository packetRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AlertResponse createAlert(Long packetId, String alertType, double confidence) {
        log.info("Creating alert for packet {} with type {} and confidence {}", packetId, alertType, confidence);

        Packet packet = packetRepository.findById(packetId)
                .orElseThrow(() -> new EntityNotFoundException("Packet not found with id: " + packetId));

        String normalizedType = normalizeAlertType(alertType);
        String severity = determineSeverity(confidence);
        String message = buildAlertMessage(packet, normalizedType, severity, confidence);
        Alert alert = createAndSaveAlert(packet, normalizedType, severity, message);
        log.info("Alert created with id: {}", alert.getId());

        return mapToResponse(alert);
    }

    @Override
    @Transactional
    public Alert createAlert(Packet packet, MLResponse mlResponse) {
        String alertType = mlResponse != null ? mlResponse.getAttackType() : null;
        double confidence = mlResponse != null && mlResponse.getConfidence() != null ? mlResponse.getConfidence() : 0.0;
        String normalizedType = normalizeAlertType(alertType);
        String severity = determineSeverity(confidence);
        String message = buildAlertMessage(packet, normalizedType, severity, confidence);
        Alert alert = createAndSaveAlert(packet, normalizedType, severity, message);
        log.info("Alert created with id: {}", alert.getId());
        return alert;
    }

    @Override
    public Page<AlertResponse> getAlerts(int page, int size, String status, String alertType) {
        log.info("Fetching alerts with page {}, size {}, status {}, alertType {}", page, size, status, alertType);

        Pageable pageable = PageRequest.of(page, size);
        Page<Alert> alerts;

        if (status != null && alertType != null) {
            alerts = alertRepository.findByStatusAndAlertType(status, alertType, pageable);
        } else if (status != null) {
            alerts = alertRepository.findByStatus(status, pageable);
        } else {
            alerts = alertRepository.findAll(pageable);
        }

        return alerts.map(this::mapToResponse);
    }

    @Override
    public AlertResponse getAlertById(Long id) {
        log.info("Fetching alert with id {}", id);

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alert not found with id: " + id));

        return mapToResponse(alert);
    }

    @Override
    @Transactional
    public AlertResponse updateAlertStatus(Long id, AlertStatusUpdateRequest request, String username) {
        log.info("Updating alert {} status to {} by user {}", id, request.getStatus(), username);

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alert not found with id: " + id));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        alert.setStatus(request.getStatus());
        alert.setResolvedBy(user);
        alert.setResolvedAt(LocalDateTime.now());
        alert.setUpdatedAt(LocalDateTime.now());

        if (request.getNotes() != null) {
            alert.setMessage(request.getNotes());
        }

        Alert updatedAlert = alertRepository.save(alert);
        log.info("Alert {} updated successfully", id);

        return mapToResponse(updatedAlert);
    }

    @Override
    @Transactional
    public AlertResponse updateStatus(Long id, String status, String resolvedBy) {
        log.info("Updating alert {} status to {} by user {}", id, status, resolvedBy);

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alert not found with id: " + id));

        if (resolvedBy != null && !resolvedBy.isBlank()) {
            User user = userRepository.findByUsername(resolvedBy)
                    .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + resolvedBy));
            alert.setResolvedBy(user);
            alert.setResolvedAt(LocalDateTime.now());
        }

        alert.setStatus(status);
        alert.setUpdatedAt(LocalDateTime.now());

        Alert updatedAlert = alertRepository.save(alert);
        log.info("Alert {} updated successfully", id);

        return mapToResponse(updatedAlert);
    }

    @Override
    @Transactional
    public void deleteAlert(Long id) {
        log.info("Deleting alert with id {}", id);

        Alert alert = alertRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Alert not found with id: " + id));

        alertRepository.delete(alert);
        log.info("Alert {} deleted successfully", id);
    }

    @Override
    public long countByStatus(String status) {
        return alertRepository.countByStatus(status);
    }

    @Override
    public List<Object[]> countGroupByAlertType() {
        return alertRepository.countGroupByAlertType();
    }

    @Override
    public List<Object[]> getTimelineSince(LocalDateTime from) {
        return alertRepository.getTimelineSince(from);
    }

    @Override
    public AlertService.AlertStatsDto getAlertStats() {
        log.info("Calculating alert statistics");

        long openCount = alertRepository.countByStatus("OPEN");
        long resolvedCount = alertRepository.countByStatus("RESOLVED");
        long ignoredCount = alertRepository.countByStatus("IGNORED");
        long totalCount = openCount + resolvedCount + ignoredCount;

        List<Object[]> alertsByType = alertRepository.countGroupByAlertType();

        return AlertService.AlertStatsDto.builder()
                .openCount(openCount)
                .resolvedCount(resolvedCount)
                .ignoredCount(ignoredCount)
                .totalCount(totalCount)
                .alertsByType(alertsByType)
                .build();
    }

    private Alert createAndSaveAlert(Packet packet, String alertType, String severity, String message) {
        Alert alert = Alert.builder()
                .packet(packet)
                .alertType(alertType)
                .severity(severity)
                .status("OPEN")
                .message(message)
                .timestamp(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return alertRepository.save(alert);
    }

    private String buildAlertMessage(Packet packet, String attackType, String severity, double confidence) {
        String srcIp = packet != null && packet.getSrcIp() != null ? packet.getSrcIp() : "unknown";
        String dstIp = packet != null && packet.getDstIp() != null ? packet.getDstIp() : "unknown";
        String attack = normalizeAlertType(attackType);
        return String.format("[%s] %s attack detected from %s -> %s (confidence: %.0f%%)",
                severity, attack, srcIp, dstIp, confidence * 100);
    }

    private String normalizeAlertType(String alertType) {
        return alertType != null && !alertType.isBlank() ? alertType : "unknown";
    }

    private String determineSeverity(double confidence) {
        if (confidence >= 0.9) {
            return "CRITICAL";
        } else if (confidence >= 0.7) {
            return "HIGH";
        } else if (confidence >= 0.5) {
            return "MEDIUM";
        } else {
            return "LOW";
        }
    }

    private AlertResponse mapToResponse(Alert alert) {
        return AlertResponse.builder()
                .id(alert.getId())
                .packetId(alert.getPacket().getId())
                .alertType(alert.getAlertType())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .status(alert.getStatus())
                .createdAt(alert.getTimestamp())
                .resolvedAt(alert.getResolvedAt())
                .resolvedByUsername(alert.getResolvedBy() != null ? alert.getResolvedBy().getUsername() : null)
                .updatedAt(alert.getUpdatedAt())
                .build();
    }
}

