# IDS ML Service — app.py
# Flask API: POST /predict, POST /predict/batch, GET /health
# Model and FeatureBuilder loaded once at startup

import logging
from datetime import datetime, timezone
from flask import Flask, request, jsonify
from flask_cors import CORS
from config import FLASK_HOST, FLASK_PORT, ALLOWED_ORIGINS
from predict import predict_single, predict_batch, is_model_loaded, get_model
from utils.feature_builder import get_feature_builder

app = Flask(__name__)
CORS(app, origins=ALLOWED_ORIGINS)
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s")
logger = logging.getLogger(__name__)


@app.before_request
def startup():
    """Load model and feature builder artifacts on first request only"""
    # Initialize both singletons
    if not is_model_loaded():
        get_model()
    get_feature_builder()


@app.route("/health", methods=["GET"])
def health():
    """Return JSON: status ok, model_loaded bool, timestamp ISO string"""
    return jsonify({
        "status": "ok",
        "model_loaded": is_model_loaded(),
        "timestamp": datetime.now(timezone.utc).isoformat()
    }), 200


@app.route("/predict", methods=["POST"])
def predict():
    """Parse JSON body, require "features" dict key, return 400 if missing"""
    data = request.get_json(silent=True)
    if not data or "features" not in data:
        return jsonify({"error": "Missing required field: features"}), 400

    try:
        # Validate input fields, build feature vector, call predict_single, log result, return 200
        packet_data = data["features"]
        builder = get_feature_builder()

        # Validate input
        missing_fields = builder.validate_input(packet_data)
        if missing_fields:
            return jsonify({
                "error": f"Missing required fields: {', '.join(missing_fields)}"
            }), 400

        # Build feature vector
        features = builder.build_feature_vector(packet_data)

        # Run prediction
        result = predict_single(features)

        # Log result
        logger.info(f"Prediction: label={result.label}, attack_type={result.attack_type}, "
                   f"confidence={result.confidence:.4f}, time_ms={result.prediction_time_ms}")

        # Return response
        return jsonify({
            "label": result.label,
            "attack_type": result.attack_type,
            "confidence": round(result.confidence, 4),
            "prediction_time_ms": result.prediction_time_ms
        }), 200

    except Exception as e:
        # Log error, return 500 with error message
        logger.error(f"Prediction error: {str(e)}", exc_info=True)
        return jsonify({"error": f"Prediction failed: {str(e)}"}), 500


@app.route("/predict/batch", methods=["POST"])
def batch():
    """Parse JSON, require "packets" list, return 400 if missing or length > 100"""
    data = request.get_json(silent=True)
    if not data or "packets" not in data:
        return jsonify({"error": "Missing required field: packets"}), 400

    packets = data["packets"]
    if not isinstance(packets, list):
        return jsonify({"error": "Field 'packets' must be a list"}), 400

    if len(packets) == 0:
        return jsonify({"error": "Packets list cannot be empty"}), 400

    if len(packets) > 100:
        return jsonify({"error": "Packets list cannot exceed 100 items"}), 400

    try:
        # Build feature vectors for all packets
        builder = get_feature_builder()
        features_list = []

        for i, packet in enumerate(packets):
            if not isinstance(packet, dict):
                return jsonify({
                    "error": f"Packet at index {i} must be a dictionary"
                }), 400

            # Validate input
            missing_fields = builder.validate_input(packet)
            if missing_fields:
                return jsonify({
                    "error": f"Packet at index {i} missing fields: {', '.join(missing_fields)}"
                }), 400

            # Build feature vector
            features = builder.build_feature_vector(packet)
            features_list.append(features)

        # Run batch prediction
        results = predict_batch(features_list)

        # Log summary
        logger.info(f"Batch prediction: {len(results)} packets processed")

        # Return response
        return jsonify({
            "predictions": [
                {
                    "label": r.label,
                    "attack_type": r.attack_type,
                    "confidence": round(r.confidence, 4),
                    "prediction_time_ms": r.prediction_time_ms
                }
                for r in results
            ],
            "total_packets": len(results)
        }), 200

    except Exception as e:
        logger.error(f"Batch prediction error: {str(e)}", exc_info=True)
        return jsonify({"error": f"Batch prediction failed: {str(e)}"}), 500


@app.errorhandler(404)
def not_found(e):
    """Handle 404 errors"""
    return jsonify({"error": "Endpoint not found"}), 404


@app.errorhandler(500)
def server_error(e):
    """Handle 500 errors"""
    return jsonify({"error": "Internal server error"}), 500


if __name__ == "__main__":
    app.run(host=FLASK_HOST, port=FLASK_PORT, debug=False)
