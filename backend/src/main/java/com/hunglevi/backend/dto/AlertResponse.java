package com.hunglevi.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertResponse {

    private Long id;

    private Long packetId;

    @JsonProperty("alertType")
    private String alertType;

    private String severity;

    private String message;

    private String status;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("resolvedAt")
    private LocalDateTime resolvedAt;

    @JsonProperty("resolvedBy")
    private String resolvedByUsername;

    @JsonProperty("updatedAt")
    private LocalDateTime updatedAt;
}

