package com.hunglevi.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacketResponse {

    private Long id;
    private String srcIp;
    private String dstIp;
    private String protocol;
    private String label;
    private String attackType;
    private Double confidence;
    private Boolean isThreat;
    private String capturedAt;
    private String message;
}
