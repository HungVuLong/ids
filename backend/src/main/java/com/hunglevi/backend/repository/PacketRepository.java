package com.hunglevi.backend.repository;

import com.hunglevi.backend.entity.Packet;
import java.time.LocalDateTime;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository

public interface PacketRepository extends JpaRepository<Packet, Long> {
    Page<Packet> findByLabel(String label, Pageable p);

    Page<Packet> findByAttackType(String type, Pageable p);

    long countByLabel(String label);

    long countByCapturedAtBetween(LocalDateTime from, LocalDateTime to);

    long countByLabelAndCapturedAtBetween(
        String label, LocalDateTime from, LocalDateTime to);
}
