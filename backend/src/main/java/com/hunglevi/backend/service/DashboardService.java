package com.hunglevi.backend.service;

import com.hunglevi.backend.dto.response.DashboardStatsResponse;
import java.time.LocalDate;

public interface DashboardService {
    DashboardStatsResponse getStats();

    DashboardStatsResponse getStatsByDateRange(LocalDate from, LocalDate to);
}

