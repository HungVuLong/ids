package com.hunglevi.backend.service;

import com.hunglevi.backend.dto.ml.MLResponse;
import com.hunglevi.backend.entity.Packet;

public interface MLService {
    MLResponse classify(Packet packet);

    boolean isHealthy();
}
