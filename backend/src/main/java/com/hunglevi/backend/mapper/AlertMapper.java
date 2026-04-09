package com.hunglevi.backend.mapper;

import com.hunglevi.backend.dto.AlertResponse;
import com.hunglevi.backend.entity.Alert;
import org.springframework.stereotype.Component;

@Component
public class AlertMapper {

    public AlertResponse toResponse(Alert alert) {
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

