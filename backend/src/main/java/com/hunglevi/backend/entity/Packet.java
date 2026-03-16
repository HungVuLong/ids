package com.hunglevi.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @Column(nullable = false, length = 45)
    private String sourceIp;

    @Column(nullable = false, length = 45)
    private String destIp;

    @Column(nullable = false, length = 10)
    private String protocol;

    @Column(nullable = false)
    private Integer size;

    @Column(nullable = false, length = 100)
    private String label;

    @Column(nullable = false, length = 50)
    private String attackType;

    @Column(nullable = false)
    private Double confidence;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime capturedAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
}
