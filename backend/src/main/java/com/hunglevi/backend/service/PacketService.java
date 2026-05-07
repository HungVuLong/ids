package com.hunglevi.backend.service;

import com.hunglevi.backend.dto.request.PacketRequest;
import com.hunglevi.backend.dto.response.PacketResponse;
import org.springframework.data.domain.Page;

public interface PacketService {

    PacketResponse analyzePacket(PacketRequest request, String submittedBy);

    Page<PacketResponse> getPackets(int page, int size, String label);
}
