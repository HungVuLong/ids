package com.hunglevi.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacketRequest {

    private String srcIp;

    private String dstIp;

    private Integer srcPort;

    private Integer dstPort;

    @NotBlank(message = "protocol is required")
    private String protocol;

    @Builder.Default
    private Double duration = 0.0;

    @Builder.Default
    private Integer land = 0;

    @Builder.Default
    private Integer wrongFragment = 0;

    @Builder.Default
    private Integer urgent = 0;
}
