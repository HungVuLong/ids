import numpy as np
import pandas as pd
import joblib
from sklearn.preprocessing import LabelEncoder, StandardScaler
from config import (
    FEATURE_COLUMNS, CATEGORICAL_COLS, ATTACK_TYPE_MAP,
    RAW_DIR, PROCESSED_DIR, ARTIFACTS_DIR,
    SCALER_PATH, LABEL_ENCODER_PATH, FEATURE_NAMES_PATH,
)


def load_raw_data(train_path, test_path):
    """Read NSL-KDD CSVs with no header, assign FEATURE_COLUMNS + label + difficulty columns"""
    df_train = pd.read_csv(train_path, header=None)
    df_test = pd.read_csv(test_path, header=None)

    # Last two columns are attack_label and difficulty_level
    column_names = FEATURE_COLUMNS + ['attack_label', 'difficulty_level']
    df_train.columns = column_names
    df_test.columns = column_names

    return df_train, df_test


def map_labels(df):
    """Add attack_type column using ATTACK_TYPE_MAP, add binary_label column (0=normal 1=attack)"""
    # Map attack labels to attack types (DoS, Probe, U2R, R2L, normal)
    df['attack_type'] = df['attack_label'].map(ATTACK_TYPE_MAP)

    # Create binary label: normal=0, all attacks=1
    df['binary_label'] = (df['attack_type'] != 'normal').astype(int)

    return df


def encode_categoricals(df_train, df_test):
    """Fit LabelEncoder on train for each col in CATEGORICAL_COLS, transform both, return encoders dict"""
    encoders = {}
    for col in CATEGORICAL_COLS:
        enc = LabelEncoder()
        df_train[col] = enc.fit_transform(df_train[col].astype(str))
        df_test[col] = enc.transform(df_test[col].astype(str))
        encoders[col] = enc

    return df_train, df_test, encoders


def scale_features(X_train, X_test):
    """Fit StandardScaler on X_train only, transform both, return scaler"""
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    return X_train_scaled, X_test_scaled, scaler


def save_artifacts(scaler, encoders):
    """Create ARTIFACTS_DIR, dump scaler to SCALER_PATH, encoders to LABEL_ENCODER_PATH"""
    ARTIFACTS_DIR.mkdir(parents=True, exist_ok=True)

    joblib.dump(scaler, SCALER_PATH)
    joblib.dump(encoders, LABEL_ENCODER_PATH)
    joblib.dump(FEATURE_COLUMNS, FEATURE_NAMES_PATH)

    print(f"✓ Artifacts saved to {ARTIFACTS_DIR}")


def save_processed_data(X_train, X_test, y_train, y_test):
    """Create PROCESSED_DIR, save each array as .npy with descriptive filename"""
    PROCESSED_DIR.mkdir(parents=True, exist_ok=True)

    np.save(PROCESSED_DIR / "X_train.npy", X_train)
    np.save(PROCESSED_DIR / "X_test.npy", X_test)
    np.save(PROCESSED_DIR / "y_train.npy", y_train)
    np.save(PROCESSED_DIR / "y_test.npy", y_test)

    print(f"✓ Processed data saved to {PROCESSED_DIR}")


def run_pipeline():
    """Run complete preprocessing pipeline"""
    # Step 1: Load raw data from RAW_DIR
    print("Step 1: Loading raw data...")
    train_path = RAW_DIR / "KDDTrain+.txt"
    test_path = RAW_DIR / "KDDTest+.txt"
    df_train, df_test = load_raw_data(train_path, test_path)
    print(f"  Train: {df_train.shape}, Test: {df_test.shape}")

    # Step 2: Map labels
    print("Step 2: Mapping attack labels...")
    df_train = map_labels(df_train)
    df_test = map_labels(df_test)
    print(f"  Attack types: {df_train['attack_type'].unique()}")

    # Step 3: Encode categoricals
    print("Step 3: Encoding categorical features...")
    df_train, df_test, encoders = encode_categoricals(df_train, df_test)
    print(f"  Encoded columns: {CATEGORICAL_COLS}")

    # Step 4: Split X/y, scale features
    print("Step 4: Scaling features...")
    X_train = df_train[FEATURE_COLUMNS].values
    X_test = df_test[FEATURE_COLUMNS].values
    y_train = df_train['binary_label'].values
    y_test = df_test['binary_label'].values

    X_train_scaled, X_test_scaled, scaler = scale_features(X_train, X_test)
    print(f"  Features scaled using StandardScaler")

    # Step 5: Save artifacts and processed data, print summary
    print("Step 5: Saving artifacts and processed data...")
    save_artifacts(scaler, encoders)
    save_processed_data(X_train_scaled, X_test_scaled, y_train, y_test)

    print("\n" + "="*60)
    print("PREPROCESSING COMPLETE")
    print("="*60)
    print(f"Training set: {X_train_scaled.shape}")
    print(f"Test set: {X_test_scaled.shape}")
    print(f"Features: {len(FEATURE_COLUMNS)}")
    print(f"Categorical columns encoded: {len(encoders)}")
    print(f"Artifacts saved to: {ARTIFACTS_DIR}")
    print(f"Processed data saved to: {PROCESSED_DIR}")


if __name__ == "__main__":
    run_pipeline()
