# Sau preprocess.py
python preprocess.py && ls data/processed/
# expect: X_train.npy  X_test.npy  y_train.npy  y_test.npy

# Sau train.py
python train.py && ls artifacts/
# expect: best_model.pkl  scaler.pkl  label_encoders.pkl  model_info.json

# Sau app.py
python app.py &
curl http://localhost:5000/health
# expect: {"status":"ok","model_loaded":true,...}

# End-to-end test
export TEST_JWT_TOKEN="VGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrZXkgZm9yIEpXVA"
python simulate_attack.py --dos 3 --normal 5 --delay 0.5
