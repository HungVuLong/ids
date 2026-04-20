# IDS ML Service — simulate_attack.py
# Sends fake NSL-KDD packets to Spring Boot backend for end-to-end testing
# Usage: python simulate_attack.py --token <jwt> --dos 5 --normal 10 --delay 0.5

import os
import time
import random
import argparse
import requests

SPRING_URL = os.getenv("SPRING_URL", "http://localhost:8080/api/packets")


def normal_packet():
    """Return realistic NSL-KDD normal traffic dict, protocol_type tcp, service http, flag SF"""
    return {
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
        "dst_host_srv_count": 10,
        "dst_host_same_srv_rate": 1.0,
        "dst_host_diff_srv_rate": 0.0,
        "dst_host_same_src_port_rate": 0.0,
        "dst_host_srv_diff_host_rate": 0.0,
        "dst_host_serror_rate": 0.0,
        "dst_host_srv_serror_rate": 0.0,
        "dst_host_rerror_rate": 0.0,
        "dst_host_srv_rerror_rate": 0.0,
    }


def dos_packet():
    """Return DoS-pattern packet, flag S0, zero bytes, high connection count"""
    return {
        "duration": 0,
        "protocol_type": "tcp",
        "service": "http",
        "flag": "S0",
        "src_bytes": 0,
        "dst_bytes": 0,
        "land": 0,
        "wrong_fragment": 0,
        "urgent": 0,
        "hot": 0,
        "num_failed_logins": 0,
        "logged_in": 0,
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
        "count": random.randint(100, 500),  # High connection count = DoS indicator
        "srv_count": random.randint(100, 500),
        "serror_rate": 0.8,  # High error rate
        "srv_serror_rate": 0.8,
        "rerror_rate": 0.0,
        "srv_rerror_rate": 0.0,
        "same_srv_rate": 1.0,
        "diff_srv_rate": 0.0,
        "srv_diff_host_rate": 0.0,
        "dst_host_count": random.randint(200, 500),
        "dst_host_srv_count": random.randint(200, 500),
        "dst_host_same_srv_rate": 0.9,
        "dst_host_diff_srv_rate": 0.0,
        "dst_host_same_src_port_rate": 0.1,
        "dst_host_srv_diff_host_rate": 0.0,
        "dst_host_serror_rate": 0.8,
        "dst_host_srv_serror_rate": 0.8,
        "dst_host_rerror_rate": 0.0,
        "dst_host_srv_rerror_rate": 0.0,
    }


def probe_packet():
    """Return Probe-pattern packet, protocol icmp, service eco_i, many dst hosts"""
    return {
        "duration": 0,
        "protocol_type": "icmp",
        "service": "eco_i",
        "flag": "SF",
        "src_bytes": 0,
        "dst_bytes": 0,
        "land": 0,
        "wrong_fragment": 0,
        "urgent": 0,
        "hot": 0,
        "num_failed_logins": 0,
        "logged_in": 0,
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
        "count": random.randint(20, 100),  # Moderate connection count
        "srv_count": random.randint(5, 30),
        "serror_rate": 0.1,
        "srv_serror_rate": 0.1,
        "rerror_rate": 0.0,
        "srv_rerror_rate": 0.0,
        "same_srv_rate": 0.3,
        "diff_srv_rate": 0.7,  # High diff_srv_rate = scanning behavior
        "srv_diff_host_rate": 0.8,  # Many different hosts = probing
        "dst_host_count": random.randint(50, 200),  # Many dest hosts
        "dst_host_srv_count": random.randint(10, 50),
        "dst_host_same_srv_rate": 0.2,
        "dst_host_diff_srv_rate": 0.8,
        "dst_host_same_src_port_rate": 0.5,
        "dst_host_srv_diff_host_rate": 0.8,
        "dst_host_serror_rate": 0.1,
        "dst_host_srv_serror_rate": 0.1,
        "dst_host_rerror_rate": 0.0,
        "dst_host_srv_rerror_rate": 0.0,
    }


def send_packet(packet, token):
    """POST packet to SPRING_URL with Bearer token, return response JSON or error dict"""
    try:
        headers = {
            "Authorization": f"Bearer {token}",
            "Content-Type": "application/json"
        }
        response = requests.post(SPRING_URL, json=packet, headers=headers, timeout=10)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        return {"error": str(e)}


def run_simulation(token, n_normal, n_dos, n_probe, delay):
    """Build mixed packet queue, shuffle, send each with delay, print label + confidence per response"""
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

    for i, (packet, expected_label) in enumerate(queue, 1):
        try:
            response = send_packet(packet, token)

            if "error" in response:
                status = "❌ Error"
                predicted = "N/A"
                confidence = "N/A"
                error_count += 1
            else:
                predicted = response.get("attack_type", "unknown")
                confidence = response.get("confidence", 0)
                match = "✓" if predicted == expected_label else "✗"
                status = f"{match} {'Match' if match == '✓' else 'Mismatch'}"
                success_count += 1

            print(f"{i:<5} {expected_label:<15} {predicted:<15} {confidence:<12.4f} {status:<15}")

            # Delay between packets
            if i < len(queue):
                time.sleep(delay)

        except Exception as e:
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
    parser.add_argument("--normal", type=int, default=10,
                       help="Number of normal packets to send")
    parser.add_argument("--dos", type=int, default=5,
                       help="Number of DoS attack packets to send")
    parser.add_argument("--probe", type=int, default=3,
                       help="Number of Probe attack packets to send")
    parser.add_argument("--delay", type=float, default=0.5,
                       help="Delay in seconds between packet sends")

    args = parser.parse_args()

    if not args.token:
        print("⚠️  Warning: No JWT token provided. Use --token or set TEST_JWT_TOKEN env var")

    run_simulation(args.token, args.normal, args.dos, args.probe, args.delay)
