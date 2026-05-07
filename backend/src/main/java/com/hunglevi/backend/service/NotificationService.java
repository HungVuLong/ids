package com.hunglevi.backend.service;

import com.hunglevi.backend.entity.Alert;

public interface NotificationService {
    void notifyAdmin(Alert alert);
}

