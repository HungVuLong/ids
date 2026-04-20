# IDS ML Service — simulate_attack.py
# Sends fake NSL-KDD packets to Spring Boot backend for end-to-end testing
# Usage: python simulate_attack.py --token <jwt> --dos 5 --normal 10 --delay 0.5

import os
import time
import random
import argparse
import requests

SPRING_URL = os.getenv("SPRING_URL", "http://localhost:1010/api/packets")


def normal_packet():
    """Return realistic normal traffic packet with required backend fields"""
    return {
        "sourceIp": f"192.168.1.{random.randint(1, 254)}",
        "destIp": f"10.0.0.{random.randint(1, 254)}",
        "protocol": "TCP",
        "size": random.randint(40, 1500),
        "label": "normal",
        "attackType": "normal",
        "confidence": round(random.uniform(0.85, 1.0), 4)
    }


def dos_packet():
    """Return DoS-pattern packet with required backend fields"""
    return {
        "sourceIp": f"192.168.1.{random.randint(1, 254)}",
        "destIp": f"10.0.0.{random.randint(1, 254)}",
        "protocol": "TCP",
        "size": random.randint(1, 100),
        "label": "attack",
        "attackType": "DoS",
        "confidence": round(random.uniform(0.75, 0.99), 4)
    }


def probe_packet():
    """Return Probe-pattern packet with required backend fields"""
    return {
        "sourceIp": f"192.168.1.{random.randint(1, 254)}",
        "destIp": f"10.0.0.{random.randint(1, 254)}",
        "protocol": "ICMP",
        "size": random.randint(40, 200),
        "label": "attack",
        "attackType": "Probe",
        "confidence": round(random.uniform(0.70, 0.95), 4)
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
