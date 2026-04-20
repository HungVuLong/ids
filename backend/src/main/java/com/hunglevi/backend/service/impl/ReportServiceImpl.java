package com.hunglevi.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunglevi.backend.dto.request.ReportRequest;
import com.hunglevi.backend.dto.response.ReportResponse;
import com.hunglevi.backend.entity.Report;
import com.hunglevi.backend.entity.User;
import com.hunglevi.backend.exception.ResourceNotFoundException;
import com.hunglevi.backend.repository.AlertRepository;
import com.hunglevi.backend.repository.PacketRepository;
import com.hunglevi.backend.repository.ReportRepository;
import com.hunglevi.backend.repository.UserRepository;
import com.hunglevi.backend.service.ReportService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final PacketRepository packetRepository;
    private final AlertRepository alertRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public ReportResponse generateReport(ReportRequest req, String generatedByUsername) {
        User generatedBy = userRepository.findByUsername(generatedByUsername)
            .orElseThrow(() -> new ResourceNotFoundException("User not found: " + generatedByUsername));

        LocalDateTime fromDateTime = req.getFromDate().atStartOfDay();
        LocalDateTime toDateTime = req.getToDate().atTime(LocalTime.MAX);

        long totalPackets = packetRepository.countByCapturedAtBetween(fromDateTime, toDateTime);
        long totalAlerts = alertRepository.countByTimestampBetween(fromDateTime, toDateTime);
        Map<String, Long> alertsByType = toMap(alertRepository.countGroupByAttackTypeBetween(fromDateTime, toDateTime));

        Snapshot snapshot = Snapshot.builder()
            .fromDate(req.getFromDate())
            .toDate(req.getToDate())
            .totalPackets(totalPackets)
            .totalAlerts(totalAlerts)
            .alertsByType(alertsByType)
            .build();

        Report report = Report.builder()
            .title(req.getTitle())
            .content(serializeSnapshot(snapshot))
            .generatedBy(generatedBy)
            .build();

        Report savedReport = reportRepository.save(report);
        log.info("Report generated: {} ({} to {})", savedReport.getId(), req.getFromDate(), req.getToDate());

        return toResponse(savedReport, snapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ReportResponse> getReports(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reportRepository.findAll(pageable)
            .map(report -> toResponse(report, deserializeSnapshot(report.getContent())));
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponse getReportById(Long id) {
        Report report = reportRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Report not found with id: " + id));
        return toResponse(report, deserializeSnapshot(report.getContent()));
    }

    @Override
    @Transactional
    public void deleteReport(Long id) {
        if (!reportRepository.existsById(id)) {
            throw new ResourceNotFoundException("Report not found with id: " + id);
        }
        reportRepository.deleteById(id);
    }

    private ReportResponse toResponse(Report report, Snapshot snapshot) {
        return ReportResponse.builder()
            .id(report.getId())
            .title(report.getTitle())
            .generatedBy(report.getGeneratedBy() != null ? report.getGeneratedBy().getUsername() : null)
            .fromDate(snapshot.getFromDate())
            .toDate(snapshot.getToDate())
            .totalPackets(snapshot.getTotalPackets())
            .totalAlerts(snapshot.getTotalAlerts())
            .alertsByType(snapshot.getAlertsByType())
            .generatedAt(report.getCreatedAt())
            .build();
    }

    private String serializeSnapshot(Snapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize report snapshot", ex);
        }
    }

    private Snapshot deserializeSnapshot(String content) {
        if (content == null || content.isBlank()) {
            return Snapshot.empty();
        }

        try {
            return objectMapper.readValue(content, new TypeReference<>() {});
        } catch (Exception ex) {
            log.warn("Failed to parse report content, returning fallback snapshot: {}", ex.getMessage());
            return Snapshot.empty();
        }
    }

    private Map<String, Long> toMap(List<Object[]> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String key = row[0] != null ? row[0].toString() : "UNKNOWN";
            long value = row[1] instanceof Number ? ((Number) row[1]).longValue() : 0L;
            result.put(key, value);
        }
        return result;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class Snapshot {
        private LocalDate fromDate;
        private LocalDate toDate;
        private long totalPackets;
        private long totalAlerts;
        private Map<String, Long> alertsByType;

        private static Snapshot empty() {
            return Snapshot.builder()
                .alertsByType(Map.of())
                .build();
        }
    }
}


