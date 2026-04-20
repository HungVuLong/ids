# IDS ML Service — predict.py
# Stateless inference module, loaded once at startup, thread-safe

import time
import numpy as np
import joblib
from dataclasses import dataclass
from config import MODEL_PATH


@dataclass
class PredictResult:
    label: str            # "normal" | "attack"
    attack_type: str      # "DoS" | "Probe" | "R2L" | "U2R" | "normal"
    confidence: float     # max class probability from predict_proba
    prediction_time_ms: int


# Module-level model cache
_model = None


def get_model():
    """Lazy load model from MODEL_PATH using joblib, cache in _model, return model"""
    global _model
    if _model is None:
        _model = joblib.load(MODEL_PATH)
    return _model


def is_model_loaded():
    """Return True if _model is not None"""
    return _model is not None


def predict_single(features):
    """Record start time, call model.predict and predict_proba on features
    Set label = "normal" if prediction == 0 else "attack"
    Set attack_type = "normal" if label is normal else map proba index to DoS/Probe/R2L/U2R
    Return PredictResult with elapsed ms"""
    model = get_model()
    start = time.time()
    prediction = model.predict(features)[0]
    proba = model.predict_proba(features)[0]
    elapsed_ms = int((time.time() - start) * 1000)

    # Determine label
    label = "normal" if prediction == 0 else "attack"

    # Determine attack_type and confidence
    if label == "normal":
        attack_type = "normal"
        confidence = proba[0]
    else:
        # Map probability index to attack type
        # Index 0 is normal, indices 1-4 map to DoS, Probe, R2L, U2R
        attack_types = ["normal", "DoS", "Probe", "R2L", "U2R"]
        max_idx = np.argmax(proba)
        attack_type = attack_types[max_idx] if max_idx < len(attack_types) else "attack"
        confidence = proba[max_idx]

    return PredictResult(
        label=label,
        attack_type=attack_type,
        confidence=float(confidence),
        prediction_time_ms=elapsed_ms
    )


def predict_batch(features_list):
    """Stack feature arrays, run single batch predict, return list of PredictResult"""
    if not features_list:
        return []

    # Stack all feature arrays into single batch
    X_batch = np.vstack(features_list)

    # Get model
    model = get_model()
    start = time.time()
    predictions = model.predict(X_batch)
    probas = model.predict_proba(X_batch)
    elapsed_ms = int((time.time() - start) * 1000)

    # Distribute elapsed time proportionally (or use same for all)
    results = []
    attack_types_map = ["normal", "DoS", "Probe", "R2L", "U2R"]

    for i, prediction in enumerate(predictions):
        label = "normal" if prediction == 0 else "attack"
        proba = probas[i]

        if label == "normal":
            attack_type = "normal"
            confidence = proba[0]
        else:
            max_idx = np.argmax(proba)
            attack_type = attack_types_map[max_idx] if max_idx < len(attack_types_map) else "attack"
            confidence = proba[max_idx]

        results.append(PredictResult(
            label=label,
            attack_type=attack_type,
            confidence=float(confidence),
            prediction_time_ms=elapsed_ms
        ))

    return results
