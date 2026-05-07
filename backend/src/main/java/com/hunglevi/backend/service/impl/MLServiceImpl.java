package com.hunglevi.backend.service.impl;

import com.hunglevi.backend.dto.ml.MLRequest;
import com.hunglevi.backend.dto.ml.MLResponse;
import com.hunglevi.backend.entity.Packet;
import com.hunglevi.backend.service.MLService;
import java.time.Duration;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class MLServiceImpl implements MLService {

    private final WebClient mlWebClient;

    @Override
    public MLResponse classify(Packet packet) {
        try {
            MLRequest request = MLRequest.fromPacket(packet);
            MLResponse response = mlWebClient.post()
                .uri("/predict")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(MLResponse.class)
                .timeout(Duration.ofSeconds(5))
                .onErrorResume(ex -> Mono.just(buildFallback()))
                .block();

            if (response == null) {
                return buildFallback();
            }

            log.info("ML classify: label={} type={} confidence={}",
                response.getLabel(), response.getAttackType(), response.getConfidence());
            return response;
        } catch (Exception e) {
            log.error("ML Service error: {}", e.getMessage());
            return buildFallback();
        }
    }

    @Override
    public boolean isHealthy() {
        try {
            HealthResponse response = mlWebClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(HealthResponse.class)
                .block(Duration.ofSeconds(3));
            return response != null && "ok".equalsIgnoreCase(response.getStatus());
        } catch (Exception e) {
            return false;
        }
    }

    private MLResponse buildFallback() {
        log.warn("ML Service unavailable — defaulting to normal classification");
        return MLResponse.builder()
            .label("normal")
            .attackType("normal")
            .confidence(0.0)
            .predictionTimeMs(0L)
            .build();
    }

    @Data
    private static class HealthResponse {
        private String status;
    }
}
