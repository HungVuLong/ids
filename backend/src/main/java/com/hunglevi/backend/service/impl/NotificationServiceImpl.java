package com.hunglevi.backend.service.impl;

import com.hunglevi.backend.dto.response.AlertNotification;
import com.hunglevi.backend.entity.Alert;
import com.hunglevi.backend.service.NotificationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void notifyAdmin(Alert alert) {
        if (alert == null) {
            log.warn("Notification skipped: alert is null");
            return;
        }

        AlertNotification notification = AlertNotification.builder()
                .alertId(alert.getId())
                .attackType(alert.getAlertType())
                .severity(alert.getSeverity())
                .message(alert.getMessage())
                .srcIp(alert.getPacket() != null ? alert.getPacket().getSrcIp() : "unknown")
                .dstIp(alert.getPacket() != null ? alert.getPacket().getDstIp() : "unknown")
                .confidence(alert.getPacket() != null ? alert.getPacket().getConfidence() : 0.0)
                .detectedAt(LocalDateTime.now().toString())
                .status("OPEN")
                .build();

        messagingTemplate.convertAndSend("/topic/alerts", notification);

        log.warn("ADMIN NOTIFIED: {} {} from {} confidence={}",
                alert.getSeverity(), alert.getAlertType(), notification.getSrcIp(), notification.getConfidence());
    }
}
