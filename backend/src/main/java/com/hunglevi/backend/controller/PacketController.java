package com.hunglevi.backend.controller;

import com.hunglevi.backend.dto.request.PacketRequest;
import com.hunglevi.backend.dto.response.PacketResponse;
import com.hunglevi.backend.service.PacketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/packets", "/api/packet"})
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")

public class PacketController {

    private final PacketService packetService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PacketResponse> createPacket(@Valid @RequestBody PacketRequest req) {
        PacketResponse response = packetService.createPacket(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<PacketResponse>> getPackets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String label
    ) {
        return ResponseEntity.ok(packetService.getPackets(page, size, label));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<PacketResponse> getPacketById(@PathVariable Long id) {
        return ResponseEntity.ok(packetService.getPacketById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePacket(@PathVariable Long id) {
        packetService.deletePacket(id);
        return ResponseEntity.noContent().build();
    }
}
