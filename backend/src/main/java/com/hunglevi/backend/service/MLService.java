package com.hunglevi.backend.service;

import com.hunglevi.backend.dto.ml.MLResponse;
import com.hunglevi.backend.entity.Packet;
import reactor.core.publisher.Mono;

public interface MLService {
    Mono<MLResponse> predict(Packet packet);

    boolean isHealthy();
}

