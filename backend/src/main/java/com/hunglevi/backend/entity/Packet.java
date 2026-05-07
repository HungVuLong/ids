package com.hunglevi.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "packets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Packet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "source_ip", length = 45)
    private String srcIp;

    @Column(name = "dest_ip", length = 45)
    private String dstIp;

    @Column(name = "src_port")
    private Integer srcPort;

    @Column(name = "dst_port")
    private Integer dstPort;

    @Column(nullable = false, length = 10)
    private String protocol;

    @Column(nullable = false)
    private Double duration;

    @Column(nullable = false)
    private Integer land;

    @Column(name = "wrong_fragment", nullable = false)
    private Integer wrongFragment;

    @Column(nullable = false)
    private Integer urgent;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(length = 50)
    private String attackType;

    @Column(nullable = false)
    private Double confidence;

    @Column(nullable = false)
    private LocalDateTime capturedAt;
}
