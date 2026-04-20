# IDS ML Service — utils/feature_builder.py
# Converts raw packet dict to scaled (1, 41) numpy array for model inference

import json
import numpy as np
import joblib
from sklearn.preprocessing import LabelEncoder, StandardScaler
from config import (
    FEATURE_COLUMNS, CATEGORICAL_COLS,
    SCALER_PATH, LABEL_ENCODER_PATH, FEATURE_NAMES_PATH,
)


class FeatureBuilder:
    """Singleton: loads scaler + encoders once, reused across all Flask requests"""

    def __init__(self):
        self._scaler = None
        self._encoders = None

    def load_artifacts(self):
        """Load scaler from SCALER_PATH, encoders from LABEL_ENCODER_PATH using joblib"""
        self._scaler = joblib.load(SCALER_PATH)
        self._encoders = joblib.load(LABEL_ENCODER_PATH)

    def validate_input(self, packet_data):
        """Return list of feature names missing from packet_data, empty list if valid"""
        missing = []
        for col in FEATURE_COLUMNS:
            if col not in packet_data:
                missing.append(col)
        return missing

    def build_feature_vector(self, packet_data):
        """Build value list in FEATURE_COLUMNS order
        For categorical cols: encode with self._encoders[col], fallback to 0 on unseen label
        For numeric cols: cast to float
        Stack into np.array, reshape to (1, 41), apply self._scaler, return"""
        values = []
        for col in FEATURE_COLUMNS:
            val = packet_data.get(col, 0)
            if col in CATEGORICAL_COLS:
                try:
                    val = self._encoders[col].transform([str(val)])[0]
                except ValueError:
                    val = 0
            else:
                val = float(val)
            values.append(val)

        # Reshape to (1, 41), scale, and return
        X = np.array(values).reshape(1, -1)
        X_scaled = self._scaler.transform(X)
        return X_scaled


# Module-level singleton — instantiated once, reused by get_feature_builder()
_builder = None


def get_feature_builder():
    """Lazy init: create FeatureBuilder, call load_artifacts(), cache in _builder"""
    global _builder
    if _builder is None:
        _builder = FeatureBuilder()
        _builder.load_artifacts()
    return _builder
