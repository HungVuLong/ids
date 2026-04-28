package com.hunglevi.backend.controller;

import com.hunglevi.backend.dto.response.DashboardStatsResponse;
import com.hunglevi.backend.service.DashboardService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:3000")

public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DashboardStatsResponse> getStats(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        if (from == null && to == null) {
            log.info("GET /api/dashboard/stats - loading default dashboard stats");
            return ResponseEntity.ok(dashboardService.getStats());
        }

        if (from == null || to == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Both 'from' and 'to' are required for date filtering");
        }

        if (from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "'from' must be before or equal to 'to'");
        }

        log.info("GET /api/dashboard/stats - loading range stats from {} to {}", from, to);
        return ResponseEntity.ok(dashboardService.getStatsByDateRange(from, to));
    }
}

