# IDS ML Service — simulate_attack.py
# Sends fake NSL-KDD packets to Spring Boot backend for end-to-end testing
# Usage: python simulate_attack.py --token <jwt> --dos 5 --normal 10 --delay 0.5

import os
import time
import random
import argparse
import requests
import json
from datetime import datetime, timezone
from config import FEATURE_COLUMNS

SPRING_URL = os.getenv("SPRING_URL", "http://localhost:1010/api/packets")
ML_PREDICT_URL = os.getenv("ML_PREDICT_URL", "http://localhost:5000/predict")
ML_BATCH_URL = os.getenv("ML_BATCH_URL", "http://localhost:5000/predict/batch")

FEATURE_DEFAULTS = {
    "duration": 0,
    "protocol_type": "tcp",
    "service": "http",
    "flag": "SF",
    "src_bytes": 215,
    "dst_bytes": 45076,
    "land": 0,
    "wrong_fragment": 0,
    "urgent": 0,
    "hot": 0,
    "num_failed_logins": 0,
    "logged_in": 1,
    "num_compromised": 0,
    "root_shell": 0,
    "su_attempted": 0,
    "num_root": 0,
    "num_file_creations": 0,
    "num_shells": 0,
    "num_access_files": 0,
    "num_outbound_cmds": 0,
    "is_host_login": 0,
    "is_guest_login": 0,
    "count": 1,
    "srv_count": 1,
    "serror_rate": 0.0,
    "srv_serror_rate": 0.0,
    "rerror_rate": 0.0,
    "srv_rerror_rate": 0.0,
    "same_srv_rate": 1.0,
    "diff_srv_rate": 0.0,
    "srv_diff_host_rate": 0.0,
    "dst_host_count": 10,
    "dst_host_srv_count": 5,
    "dst_host_same_srv_rate": 0.9,
    "dst_host_diff_srv_rate": 0.1,
    "dst_host_same_src_port_rate": 0.0,
    "dst_host_srv_diff_host_rate": 0.0,
    "dst_host_serror_rate": 0.0,
    "dst_host_srv_serror_rate": 0.0,
    "dst_host_rerror_rate": 0.0,
    "dst_host_srv_rerror_rate": 0.0,
}


def normal_packet():
    """Return realistic normal traffic packet with required backend fields"""
    return build_backend_packet("normal")


def dos_packet():
    """Return DoS-pattern packet with required backend fields"""
    return build_backend_packet("dos")


def probe_packet():
    """Return Probe-pattern packet with required backend fields"""
    return build_backend_packet("probe")


def build_backend_packet(kind):
    """Return a backend packet dict matching the Spring schema."""
    now = datetime.now(timezone.utc).isoformat(timespec="seconds")
    protocol = "TCP" if kind in {"normal", "dos"} else "ICMP"
    src_port = random.randint(1024, 65535)
    dst_port = 80 if protocol == "TCP" else 0

    base = {
        "srcIp": f"192.168.1.{random.randint(1, 254)}",
        "dstIp": f"10.0.0.{random.randint(1, 254)}",
        "srcPort": src_port,
        "dstPort": dst_port,
        "protocol": protocol,
        "duration": 0.0,
        "land": 0,
        "wrongFragment": 0,
        "urgent": 0,
        "capturedAt": now,
    }

    if kind == "normal":
        base.update({
            "size": random.randint(40, 1500),
            "label": "normal",
            "attackType": "normal",
            "confidence": round(random.uniform(0.85, 1.0), 4),
        })
    elif kind == "dos":
        base.update({
            "size": random.randint(1, 100),
            "label": "attack",
            "attackType": "DoS",
            "confidence": round(random.uniform(0.75, 0.99), 4),
        })
    else:
        base.update({
            "size": random.randint(40, 200),
            "label": "attack",
            "attackType": "Probe",
            "confidence": round(random.uniform(0.70, 0.95), 4),
        })

    return base


def map_backend_to_features(packet):
    """Map backend packet fields into the 41 NSL-KDD feature set."""
    features = {k: FEATURE_DEFAULTS[k] for k in FEATURE_COLUMNS}

    protocol = str(packet.get("protocol", "TCP")).lower()
    if protocol in {"tcp", "udp", "icmp"}:
        features["protocol_type"] = protocol

    features["duration"] = float(packet.get("duration", 0))
    features["land"] = int(packet.get("land", 0))
    features["wrong_fragment"] = int(packet.get("wrongFragment", 0))
    features["urgent"] = int(packet.get("urgent", 0))

    size = float(packet.get("size", features["src_bytes"]))
    features["src_bytes"] = size
    features["dst_bytes"] = 0.0

    return features


def build_feature_packet(kind):
    """Return a 41-feature NSL-KDD packet dict for ML /predict endpoints."""
    features = {k: FEATURE_DEFAULTS[k] for k in FEATURE_COLUMNS}

    if kind == "dos":
        features.update({
            "flag": "S0",
            "src_bytes": 0,
            "dst_bytes": 0,
            "count": 500,
            "srv_count": 500,
            "serror_rate": 1.0,
            "srv_serror_rate": 1.0,
            "dst_host_count": 255,
            "dst_host_srv_count": 255,
            "dst_host_serror_rate": 1.0,
            "dst_host_srv_serror_rate": 1.0,
        })
    elif kind == "probe":
        features.update({
            "protocol_type": "icmp",
            "service": "eco_i",
            "flag": "SF",
            "src_bytes": 100,
            "dst_bytes": 0,
            "count": 45,
            "srv_count": 42,
            "same_srv_rate": 0.8,
            "diff_srv_rate": 0.2,
            "srv_diff_host_rate": 0.15,
            "dst_host_count": 128,
            "dst_host_srv_count": 120,
            "dst_host_same_srv_rate": 0.7,
            "dst_host_diff_srv_rate": 0.3,
            "dst_host_same_src_port_rate": 0.1,
            "dst_host_srv_diff_host_rate": 0.25,
        })

    return features


def send_packet(packet, token, url):
    """POST packet to url with optional Bearer token, return response JSON or error dict"""
    try:
        headers = {"Content-Type": "application/json"}
        if token:
            headers["Authorization"] = f"Bearer {token}"
        response = requests.post(url, json=packet, headers=headers, timeout=10)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        return {"error": str(e)}


def parse_json_response(raw):
    """Normalize response payload into dict/list or return error dict."""
    if isinstance(raw, (dict, list)):
        return raw
    if isinstance(raw, str):
        try:
            return json.loads(raw)
        except json.JSONDecodeError:
            return {"error": raw}
    return {"error": f"Unexpected response type: {type(raw).__name__}"}


def get_prediction_fields(result):
    """Extract predicted label and confidence from a result dict."""
    if not isinstance(result, dict):
        return "unknown", 0.0
    return result.get("attack_type", "unknown"), result.get("confidence", 0)


def run_simulation(token, n_normal, n_dos, n_probe, delay, mode, use_batch, use_backend_packets):
    """Build mixed packet queue, shuffle, send each with delay, print label + confidence per response"""
    if mode == "ml":
        if use_backend_packets:
            queue = (
                [(build_backend_packet("normal"), "normal")] * n_normal +
                [(build_backend_packet("dos"), "DoS")] * n_dos +
                [(build_backend_packet("probe"), "Probe")] * n_probe
            )
        else:
            queue = (
                [(build_feature_packet("normal"), "normal")] * n_normal +
                [(build_feature_packet("dos"), "DoS")] * n_dos +
                [(build_feature_packet("probe"), "Probe")] * n_probe
            )
    else:
        queue = (
            [(normal_packet(), "normal")] * n_normal +
            [(dos_packet(), "DoS")] * n_dos +
            [(probe_packet(), "Probe")] * n_probe
        )
    random.shuffle(queue)

    print(f"\n{'='*70}")
    print(f"IDS Attack Simulator - Starting {len(queue)} packet(s)")
    print(f"{'='*70}")
    print(f"{'#':<5} {'Expected':<15} {'Predicted':<15} {'Confidence':<12} {'Status':<15}")
    print(f"{'-'*70}")

    success_count = 0
    error_count = 0

    if mode == "ml" and use_batch:
        if use_backend_packets:
            batch_payload = {"packets": [map_backend_to_features(packet) for packet, _ in queue]}
        else:
            batch_payload = {"packets": [packet for packet, _ in queue]}
        response = send_packet(batch_payload, token, ML_BATCH_URL)
        response = parse_json_response(response)

        if "error" in response:
            print(f"{1:<5} {'batch':<15} {'N/A':<15} {'N/A':<12} ❌ Error")
            error_count = len(queue)
        else:
            results = response
            if isinstance(response, dict):
                results = response.get("results") or response.get("predictions") or []
            for i, (result, (_, expected_label)) in enumerate(zip(results, queue), 1):
                predicted, confidence = get_prediction_fields(result)
                match = "✓" if predicted == expected_label else "✗"
                status = f"{match} {'Match' if match == '✓' else 'Mismatch'}"
                success_count += 1
                print(f"{i:<5} {expected_label:<15} {predicted:<15} {confidence:<12.4f} {status:<15}")
    else:
        for i, (packet, expected_label) in enumerate(queue, 1):
            try:
                if mode == "ml":
                    payload = packet
                    if use_backend_packets:
                        payload = map_backend_to_features(packet)
                    response = send_packet({"features": payload}, token, ML_PREDICT_URL)
                else:
                    response = send_packet(packet, token, SPRING_URL)

                response = parse_json_response(response)

                if "error" in response:
                    status = "❌ Error"
                    predicted = "N/A"
                    confidence = "N/A"
                    error_count += 1
                else:
                    predicted, confidence = get_prediction_fields(response)
                    match = "✓" if predicted == expected_label else "✗"
                    status = f"{match} {'Match' if match == '✓' else 'Mismatch'}"
                    success_count += 1

                print(f"{i:<5} {expected_label:<15} {predicted:<15} {confidence:<12.4f} {status:<15}")

                # Delay between packets
                if i < len(queue):
                    time.sleep(delay)

            except Exception:
                print(f"{i:<5} {expected_label:<15} {'Error':<15} {'N/A':<12} ❌ Exception")
                error_count += 1
                if i < len(queue):
                    time.sleep(delay)

    print(f"{'-'*70}")
    print(f"\nSimulation Complete:")
    print(f"  Total packets sent: {len(queue)}")
    print(f"  Successful: {success_count}")
    print(f"  Errors: {error_count}")
    print(f"{'='*70}\n")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="IDS Attack Simulator")
    parser.add_argument("--token", default=os.getenv("TEST_JWT_TOKEN", ""),
                       help="JWT token for authentication")
    parser.add_argument("--mode", choices=["spring", "ml"], default="spring",
                       help="Target backend: spring (api/packets) or ml (/predict)")
    parser.add_argument("--batch", action="store_true",
                       help="Use /predict/batch when --mode=ml")
    parser.add_argument("--use-backend-packets", action="store_true",
                       help="Generate backend packets and map them to ML features")
    parser.add_argument("--normal", type=int, default=10,
                       help="Number of normal packets to send")
    parser.add_argument("--dos", type=int, default=5,
                       help="Number of DoS attack packets to send")
    parser.add_argument("--probe", type=int, default=3,
                       help="Number of Probe attack packets to send")
    parser.add_argument("--delay", type=float, default=0.5,
                       help="Delay in seconds between packet sends")

    args = parser.parse_args()

    if not args.token and args.mode == "spring":
        print("⚠️  Warning: No JWT token provided. Use --token or set TEST_JWT_TOKEN env var")

    run_simulation(
        args.token,
        args.normal,
        args.dos,
        args.probe,
        args.delay,
        args.mode,
        args.batch,
        args.use_backend_packets,
    )
