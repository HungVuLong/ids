package com.hunglevi.backend.service.impl;

import com.hunglevi.backend.dto.request.PacketRequest;
import com.hunglevi.backend.dto.response.PacketResponse;
import com.hunglevi.backend.entity.Packet;
import com.hunglevi.backend.exception.ResourceNotFoundException;
import com.hunglevi.backend.repository.PacketRepository;
import com.hunglevi.backend.service.PacketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PacketServiceImpl implements PacketService {

    private final PacketRepository packetRepository;

    @Override
    @Transactional
    public PacketResponse createPacket(PacketRequest req) {
        Packet packet = Packet.builder()
                .sourceIp(req.getSourceIp())
                .destIp(req.getDestIp())
                .protocol(req.getProtocol())
                .size(req.getSize())
                .label(req.getLabel())
                .attackType(req.getAttackType())
                .confidence(req.getConfidence())
                .build();

        Packet saved = packetRepository.save(packet);
        return toResponse(saved);
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

    @Override
    @Transactional(readOnly = true)
    public PacketResponse getPacketById(Long id) {
        Packet packet = packetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Packet not found with id: " + id));
        return toResponse(packet);
    }

    @Override
    @Transactional
    public void deletePacket(Long id) {
        if (!packetRepository.existsById(id)) {
            throw new ResourceNotFoundException("Packet not found with id: " + id);
        }
        packetRepository.deleteById(id);
    }

    private PacketResponse toResponse(Packet packet) {
        return PacketResponse.builder()
                .id(packet.getId())
                .sourceIp(packet.getSourceIp())
                .destIp(packet.getDestIp())
                .protocol(packet.getProtocol())
                .size(packet.getSize())
                .label(packet.getLabel())
                .attackType(packet.getAttackType())
                .confidence(packet.getConfidence())
                .capturedAt(packet.getCapturedAt())
                .build();
    }
}

