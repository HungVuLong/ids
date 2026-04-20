package com.hunglevi.backend.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacketRequest {

    @NotBlank(message = "sourceIp is required")
    private String sourceIp;

    @NotBlank(message = "destIp is required")
    private String destIp;

    @NotBlank(message = "protocol is required")
    private String protocol;

    @NotNull(message = "size is required")
    @Min(value = 1, message = "size must be greater than 0")
    private Integer size;

    @NotBlank(message = "label is required")
    private String label;

    @NotBlank(message = "attackType is required")
    private String attackType;

    @NotNull(message = "confidence is required")
    private Double confidence;
}

