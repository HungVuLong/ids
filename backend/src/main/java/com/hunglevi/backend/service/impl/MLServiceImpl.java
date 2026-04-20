package com.hunglevi.backend.service.impl;

import com.hunglevi.backend.dto.ml.MLRequest;
import com.hunglevi.backend.dto.ml.MLResponse;
import com.hunglevi.backend.entity.Packet;
import com.hunglevi.backend.service.MLService;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
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

    private static final MLResponse FALLBACK_RESPONSE = MLResponse.builder()
        .label("unknown")
        .attackType("UNKNOWN")
        .confidence(0.0)
        .predictionTimeMs(0L)
        .build();

    private final WebClient mlWebClient;

    @Override
    public Mono<MLResponse> predict(Packet packet) {
        Map<String, Object> features = buildFeatures(packet);

        return mlWebClient.post()
            .uri("/predict")
            .bodyValue(new MLRequest(features))
            .retrieve()
            .bodyToMono(MLResponse.class)
            // ML dependency must never crash alerting; use safe fallback values.
            .onErrorReturn(FALLBACK_RESPONSE)
            .doOnNext(response -> log.info("ML predict: {}", response.getLabel()));
    }

    @Override
    public boolean isHealthy() {
        return Boolean.TRUE.equals(
            mlWebClient.get()
                .uri("/health")
                .retrieve()
                .bodyToMono(HealthResponse.class)
                .map(response -> "ok".equalsIgnoreCase(response.getStatus()))
                .onErrorReturn(false)
                .block(Duration.ofSeconds(3))
        );
    }

    private Map<String, Object> buildFeatures(Packet packet) {
        Map<String, Object> features = new LinkedHashMap<>();
        features.put("sourceIp", packet.getSourceIp());
        features.put("destIp", packet.getDestIp());
        features.put("protocol", packet.getProtocol());
        features.put("size", packet.getSize());

        if (packet.getCapturedAt() != null) {
            features.put("capturedAt", packet.getCapturedAt().toString());
        }

        return features;
    }

    @Data
    private static class HealthResponse {
        private String status;
    }
}

