from pathlib import Path

# Base directory is the parent of this file
BASE_DIR = Path(__file__).parent

# Data directories relative to BASE_DIR
DATA_DIR = BASE_DIR / "data"
RAW_DIR = DATA_DIR / "raw"
PROCESSED_DIR = DATA_DIR / "processed"
ARTIFACTS_DIR = BASE_DIR / "artifacts"

# NSL-KDD 41 feature column names in order
FEATURE_COLUMNS = [
    "duration", "protocol_type", "service", "flag", "src_bytes", "dst_bytes",
    "land", "wrong_fragment", "urgent", "hot", "num_failed_logins", "logged_in",
    "num_compromised", "root_shell", "su_attempted", "num_root", "num_file_creations",
    "num_shells", "num_access_files", "num_outbound_cmds", "is_host_login",
    "is_guest_login", "count", "srv_count", "serror_rate", "srv_serror_rate",
    "rerror_rate", "srv_rerror_rate", "same_srv_rate", "diff_srv_rate",
    "srv_diff_host_rate", "dst_host_count", "dst_host_srv_count",
    "dst_host_same_srv_rate", "dst_host_diff_srv_rate", "dst_host_same_src_port_rate",
    "dst_host_srv_diff_host_rate", "dst_host_serror_rate", "dst_host_srv_serror_rate",
    "dst_host_rerror_rate", "dst_host_srv_rerror_rate"
]

# Categorical columns that need LabelEncoding
CATEGORICAL_COLS = ["protocol_type", "service", "flag"]

# Map original NSL-KDD attack labels to 4 categories + normal
ATTACK_TYPE_MAP = {
    "normal": "normal",
    "neptune": "DoS",
    "smurf": "DoS",
    "land": "DoS",
    "pod": "DoS",
    "back": "DoS",
    "teardrop": "DoS",
    "udpstorm": "DoS",
    "process_table": "DoS",
    "mailbomb": "DoS",
    "port_sweep": "Probe",
    "nmap": "Probe",
    "ipsweep": "Probe",
    "satan": "Probe",
    "saint": "Probe",
    "portsweep": "Probe",
    "mscan": "Probe",
    "spy": "Probe",
    "nnmap": "Probe",
    "buffer_overflow": "U2R",
    "loadmodule": "U2R",
    "rootkit": "U2R",
    "perl": "U2R",
    "xterm": "U2R",
    "ps": "U2R",
    "httptunnel": "U2R",
    "ftp_write": "R2L",
    "phf": "R2L",
    "guess_passwd": "R2L",
    "imap": "R2L",
    "multihop": "R2L",
    "warezmaster": "R2L",
    "warezclient": "R2L",
    "snmpgetattack": "R2L",
    "named": "R2L",
    "sendmail": "R2L",
    "xsnoop": "R2L",
    "xlock": "R2L",
    "worm": "R2L",
    "rsync": "R2L",
    "snmpguess": "R2L",
}

# Binary label: normal=0, all attacks=1
BINARY_MAP = {
    "normal": 0,
    "DoS": 1,
    "Probe": 1,
    "U2R": 1,
    "R2L": 1
}

# Random Forest hyperparameters
RF_PARAMS = {"n_estimators": 100, "random_state": 42, "max_depth": 15, "min_samples_split": 5, "min_samples_leaf": 2}

# XGBoost hyperparameters
XGB_PARAMS = {"n_estimators": 100, "learning_rate": 0.1, "max_depth": 5, "min_child_weight": 1, "subsample": 0.8}

# Artifact file paths
MODEL_PATH = ARTIFACTS_DIR / "model.pkl"
SCALER_PATH = ARTIFACTS_DIR / "scaler.pkl"
LABEL_ENCODER_PATH = ARTIFACTS_DIR / "label_encoder.pkl"
FEATURE_NAMES_PATH = ARTIFACTS_DIR / "feature_names.pkl"

# Flask settings
FLASK_HOST = "0.0.0.0"
FLASK_PORT = 5000
ALLOWED_ORIGINS = ["http://localhost:3000", "http://localhost:8080"]
