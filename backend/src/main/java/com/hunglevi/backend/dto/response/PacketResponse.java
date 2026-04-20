package com.hunglevi.backend.dto.response;

import java.time.LocalDateTime;
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
    private String sourceIp;
    private String destIp;
    private String protocol;
    private Integer size;
    private String label;
    private String attackType;
    private Double confidence;
    private LocalDateTime capturedAt;
}

