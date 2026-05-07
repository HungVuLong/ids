package com.hunglevi.backend.dto.ml;

import com.hunglevi.backend.entity.Packet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MLRequest {
    private Map<String, Object> features;

    private static final List<String> NSL_KDD_FEATURES = List.of(
            "duration",
            "protocol_type",
            "service",
            "flag",
            "src_bytes",
            "dst_bytes",
            "land",
            "wrong_fragment",
            "urgent",
            "hot",
            "num_failed_logins",
            "logged_in",
            "num_compromised",
            "root_shell",
            "su_attempted",
            "num_root",
            "num_file_creations",
            "num_shells",
            "num_access_files",
            "num_outbound_cmds",
            "is_host_login",
            "is_guest_login",
            "count",
            "srv_count",
            "serror_rate",
            "srv_serror_rate",
            "rerror_rate",
            "srv_rerror_rate",
            "same_srv_rate",
            "diff_srv_rate",
            "srv_diff_host_rate",
            "dst_host_count",
            "dst_host_srv_count",
            "dst_host_same_srv_rate",
            "dst_host_diff_srv_rate",
            "dst_host_same_src_port_rate",
            "dst_host_srv_diff_host_rate",
            "dst_host_serror_rate",
            "dst_host_srv_serror_rate",
            "dst_host_rerror_rate",
            "dst_host_srv_rerror_rate"
    );

    public static MLRequest fromPacket(Packet packet) {
        Map<String, Object> features = new LinkedHashMap<>();
        for (String name : NSL_KDD_FEATURES) {
            features.put(name, 0);
        }

        if (packet != null) {
            features.put("duration", packet.getDuration() != null ? packet.getDuration() : 0);
            features.put("protocol_type", packet.getProtocol() != null ? packet.getProtocol() : "tcp");
            features.put("land", packet.getLand() != null ? packet.getLand() : 0);
            features.put("wrong_fragment", packet.getWrongFragment() != null ? packet.getWrongFragment() : 0);
            features.put("urgent", packet.getUrgent() != null ? packet.getUrgent() : 0);
        } else {
            features.put("protocol_type", "tcp");
        }

        return MLRequest.builder().features(features).build();
    }
}
