package com.hunglevi.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertStatusUpdateRequest {

    @NotBlank(message = "Status cannot be blank")
    @Pattern(regexp = "RESOLVED|IGNORED", message = "Status must be RESOLVED or IGNORED")
    private String status;

    private String notes;
}

