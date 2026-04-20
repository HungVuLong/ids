package com.hunglevi.backend.service;

import com.hunglevi.backend.dto.request.ReportRequest;
import com.hunglevi.backend.dto.response.ReportResponse;
import org.springframework.data.domain.Page;

public interface ReportService {
    ReportResponse generateReport(ReportRequest req, String generatedByUsername);

    Page<ReportResponse> getReports(int page, int size);

    ReportResponse getReportById(Long id);

    void deleteReport(Long id);
}

