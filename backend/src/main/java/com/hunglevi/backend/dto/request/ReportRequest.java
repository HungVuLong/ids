package com.hunglevi.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {

    @NotBlank(message = "title is required")
    private String title;

    @NotNull(message = "fromDate is required")
    private LocalDate fromDate;

    @NotNull(message = "toDate is required")
    private LocalDate toDate;

    @JsonIgnore
    @jakarta.validation.constraints.AssertTrue(message = "toDate must be after or equal to fromDate")
    public boolean isDateOrderValid() {
        if (fromDate == null || toDate == null) {
            return true;
        }
        return !toDate.isBefore(fromDate);
    }

    @JsonIgnore
    @jakarta.validation.constraints.AssertTrue(message = "date range must not exceed 31 days")
    public boolean isMaxRangeValid() {
        if (fromDate == null || toDate == null || toDate.isBefore(fromDate)) {
            return true;
        }
        return !fromDate.plusDays(31).isBefore(toDate);
    }
}

