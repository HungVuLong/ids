package com.hunglevi.backend.websocket;

import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AlertWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/alerts/subscribe")
    public void handleSubscription(Principal principal) {
        String subscriber = principal != null ? principal.getName() : "anonymous";
        // Keep a lightweight server-side trace of who subscribes to alerts.
        log.info("Alert subscriber connected: {}", subscriber);

        if (messagingTemplate != null) {
            log.debug("Subscription accepted for {}", subscriber);
        }
    }
}

