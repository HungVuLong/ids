package com.hunglevi.backend.dto.request;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ReportRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldFailWhenToDateBeforeFromDate() {
        ReportRequest req = ReportRequest.builder()
            .title("Weekly report")
            .fromDate(LocalDate.of(2026, 4, 10))
            .toDate(LocalDate.of(2026, 4, 9))
            .build();

        var violations = validator.validate(req);

        assertThat(violations)
            .extracting(v -> v.getMessage())
            .contains("toDate must be after or equal to fromDate");
    }

    @Test
    void shouldFailWhenRangeExceeds31Days() {
        ReportRequest req = ReportRequest.builder()
            .title("Monthly report")
            .fromDate(LocalDate.of(2026, 1, 1))
            .toDate(LocalDate.of(2026, 2, 5))
            .build();

        var violations = validator.validate(req);

        assertThat(violations)
            .extracting(v -> v.getMessage())
            .contains("date range must not exceed 31 days");
    }

    @Test
    void shouldPassWhenRangeIsValid() {
        ReportRequest req = ReportRequest.builder()
            .title("Valid report")
            .fromDate(LocalDate.of(2026, 4, 1))
            .toDate(LocalDate.of(2026, 4, 30))
            .build();

        var violations = validator.validate(req);

        assertThat(violations).isEmpty();
    }
}

