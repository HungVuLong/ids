package com.hunglevi.backend.service.impl;

import com.hunglevi.backend.dto.ml.MLResponse;
import com.hunglevi.backend.dto.request.PacketRequest;
import com.hunglevi.backend.dto.response.PacketResponse;
import com.hunglevi.backend.entity.Alert;
import com.hunglevi.backend.entity.Packet;
import com.hunglevi.backend.repository.PacketRepository;
import com.hunglevi.backend.service.AlertService;
import com.hunglevi.backend.service.MLService;
import com.hunglevi.backend.service.NotificationService;
import com.hunglevi.backend.service.PacketService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PacketServiceImpl implements PacketService {

    private static final DateTimeFormatter ISO_DATE_TIME = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final PacketRepository packetRepository;
    private final MLService mlService;
    private final AlertService alertService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PacketResponse analyzePacket(PacketRequest request, String submittedBy) {
        Packet packet = Packet.builder()
                .srcIp(request.getSrcIp())
                .dstIp(request.getDstIp())
                .srcPort(request.getSrcPort())
                .dstPort(request.getDstPort())
                .protocol(request.getProtocol())
                .duration(defaultDouble(request.getDuration()))
                .land(defaultInt(request.getLand()))
                .wrongFragment(defaultInt(request.getWrongFragment()))
                .urgent(defaultInt(request.getUrgent()))
                .label("pending")
                .attackType(null)
                .confidence(0.0)
                .capturedAt(LocalDateTime.now())
                .build();

        packet = packetRepository.save(packet);

        MLResponse mlResult = mlService.classify(packet);
        if (mlResult == null) {
            mlResult = MLResponse.builder()
                    .label("unknown")
                    .attackType(null)
                    .confidence(0.0)
                    .build();
        }

        packet.setLabel(mlResult.getLabel());
        packet.setAttackType(mlResult.getAttackType());
        packet.setConfidence(mlResult.getConfidence());
        packetRepository.save(packet);

        if ("attack".equalsIgnoreCase(mlResult.getLabel())) {
            log.warn("THREAT DETECTED: {} with confidence {}", mlResult.getAttackType(), mlResult.getConfidence());
            Alert alert = alertService.createAlert(packet, mlResult);
            notificationService.notifyAdmin(alert);
        }

        return toResponse(packet);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PacketResponse> getPackets(int page, int size, String label) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Packet> packets;

        if (label != null && !label.isBlank()) {
            packets = packetRepository.findByLabel(label, pageable);
        } else {
            packets = packetRepository.findAll(pageable);
        }

        return packets.map(this::toResponse);
    }

    private PacketResponse toResponse(Packet packet) {
        boolean isThreat = "attack".equalsIgnoreCase(packet.getLabel());
        String message = isThreat
                ? String.format("%s attack detected with %d%% confidence",
                packet.getAttackType(), Math.round(packet.getConfidence() * 100))
                : "Normal traffic detected";

        return PacketResponse.builder()
                .id(packet.getId())
                .srcIp(packet.getSrcIp())
                .dstIp(packet.getDstIp())
                .protocol(packet.getProtocol())
                .label(packet.getLabel())
                .attackType(packet.getAttackType())
                .confidence(packet.getConfidence())
                .isThreat(isThreat)
                .capturedAt(packet.getCapturedAt() != null ? packet.getCapturedAt().format(ISO_DATE_TIME) : null)
                .message(message)
                .build();
    }

    private static Double defaultDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private static Integer defaultInt(Integer value) {
        return value != null ? value : 0;
    }
}
