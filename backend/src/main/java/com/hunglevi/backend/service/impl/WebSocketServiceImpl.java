package com.hunglevi.backend.service.impl;

import com.hunglevi.backend.dto.AlertResponse;
import com.hunglevi.backend.entity.Alert;
import com.hunglevi.backend.mapper.AlertMapper;
import com.hunglevi.backend.service.WebSocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketServiceImpl implements WebSocketService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AlertMapper mapper;

    @Override
    public void pushAlert(Alert alert) {
        AlertResponse response = mapper.toResponse(alert);
        messagingTemplate.convertAndSend("/topic/alerts", response);
        log.info("WebSocket push: id={} severity={} at {}", response.getId(), response.getSeverity(), response.getCreatedAt());
    }
}

