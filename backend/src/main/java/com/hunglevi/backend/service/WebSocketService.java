package com.hunglevi.backend.service;

import com.hunglevi.backend.entity.Alert;

public interface WebSocketService {
    void pushAlert(Alert alert);
}

