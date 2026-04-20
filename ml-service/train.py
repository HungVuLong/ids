# IDS ML Service — train.py
# Train Random Forest and XGBoost, compare by F1, save best model to artifacts

import time
import json
import numpy as np
import joblib
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import f1_score, accuracy_score, precision_score, recall_score, classification_report
from xgboost import XGBClassifier
from config import PROCESSED_DIR, ARTIFACTS_DIR, MODEL_PATH, RF_PARAMS, XGB_PARAMS


def load_processed_data():
    """Load X_train, X_test, y_train, y_test from PROCESSED_DIR using np.load"""
    X_train = np.load(PROCESSED_DIR / "X_train.npy")
    X_test = np.load(PROCESSED_DIR / "X_test.npy")
    y_train = np.load(PROCESSED_DIR / "y_train.npy")
    y_test = np.load(PROCESSED_DIR / "y_test.npy")

    return X_train, X_test, y_train, y_test


def train_random_forest(X_train, y_train):
    """Fit RandomForestClassifier using RF_PARAMS, print elapsed time, return model and elapsed seconds"""
    start = time.time()
    model = RandomForestClassifier(**RF_PARAMS)
    model.fit(X_train, y_train)
    elapsed = time.time() - start

    print(f"✓ Random Forest trained in {elapsed:.2f}s")
    return model, elapsed


def train_xgboost(X_train, y_train):
    """Fit XGBClassifier using XGB_PARAMS, print elapsed time, return model and elapsed seconds"""
    start = time.time()
    model = XGBClassifier(**XGB_PARAMS, verbosity=0)
    model.fit(X_train, y_train)
    elapsed = time.time() - start

    print(f"✓ XGBoost trained in {elapsed:.2f}s")
    return model, elapsed


def evaluate_model(model, X_test, y_test, name, train_time):
    """Predict, compute accuracy/f1/precision/recall, print classification_report
    Return dict with keys: name, accuracy, f1, precision, recall, train_time"""
    y_pred = model.predict(X_test)

    accuracy = accuracy_score(y_test, y_pred)
    f1 = f1_score(y_test, y_pred)
    precision = precision_score(y_test, y_pred)
    recall = recall_score(y_test, y_pred)

    print(f"\n{name} Classification Report:")
    print(classification_report(y_test, y_pred, target_names=["Normal", "Attack"]))

    return {
        "name": name,
        "accuracy": accuracy,
        "f1": f1,
        "precision": precision,
        "recall": recall,
        "train_time": train_time
    }


def compare_and_select_best(results):
    """Print table: Model | Accuracy | F1 | Precision | Recall | Time
    Return result dict with highest f1 score"""
    print(f"\n{'Model':<20} {'Accuracy':>10} {'F1':>10} {'Precision':>10} {'Recall':>10} {'Time':>8}")
    print("-" * 70)
    for r in results:
        print(f"{r['name']:<20} {r['accuracy']:>10.4f} {r['f1']:>10.4f} {r['precision']:>10.4f} {r['recall']:>10.4f} {r['train_time']:>8.2f}s")

    # Return best by F1 score
    best = max(results, key=lambda x: x['f1'])
    print(f"\n✓ Best model selected: {best['name']} (F1={best['f1']:.4f})")
    return best


def save_best_model(model, result):
    """Create ARTIFACTS_DIR, dump model to MODEL_PATH, save result as model_info.json"""
    ARTIFACTS_DIR.mkdir(parents=True, exist_ok=True)

    # Save model
    joblib.dump(model, MODEL_PATH)

    # Save model info
    model_info = {
        "model_name": result['name'],
        "accuracy": float(result['accuracy']),
        "f1": float(result['f1']),
        "precision": float(result['precision']),
        "recall": float(result['recall']),
        "train_time": float(result['train_time'])
    }

    with open(ARTIFACTS_DIR / "model_info.json", 'w') as f:
        json.dump(model_info, f, indent=2)

    print(f"✓ Best model saved to {MODEL_PATH}")
    print(f"✓ Model info saved to {ARTIFACTS_DIR / 'model_info.json'}")


def run_training():
    """Load processed data, train both models, evaluate, compare, save best"""
    print("="*60)
    print("TRAINING IDS ML MODELS")
    print("="*60)

    # Load processed data
    print("\nLoading processed data...")
    X_train, X_test, y_train, y_test = load_processed_data()
    print(f"✓ Data loaded: X_train={X_train.shape}, X_test={X_test.shape}")

    # Train Random Forest
    print("\nTraining Random Forest...")
    rf_model, rf_time = train_random_forest(X_train, y_train)

    # Train XGBoost
    print("Training XGBoost...")
    xgb_model, xgb_time = train_xgboost(X_train, y_train)

    # Evaluate both models
    print("\nEvaluating models...")
    rf_result = evaluate_model(rf_model, X_test, y_test, "Random Forest", rf_time)
    xgb_result = evaluate_model(xgb_model, X_test, y_test, "XGBoost", xgb_time)

    # Compare and select best
    results = [rf_result, xgb_result]
    best_result = compare_and_select_best(results)

    # Save best model
    best_model = rf_model if best_result['name'] == "Random Forest" else xgb_model
    save_best_model(best_model, best_result)

    print("\n" + "="*60)
    print("TRAINING COMPLETE")
    print("="*60)


if __name__ == "__main__":
    run_training()
