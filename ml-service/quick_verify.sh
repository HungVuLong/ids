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
$env:TEST_JWT_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJodW5nbGV2aTEiLCJpYXQiOjE3ODAxOTU5NjAsImV4cCI6MTc4MDI4MjM2MH0.o3IAXGqIzoChew5NPwTQ4hHvQC1mNeL02nxAxwxRo7fKZt3WnmugoIRiHgmHAQPGqHfUuKh-_AH26WJbrKUjmw"; $env:SPRING_URL = "http://backend:1010/api/packets"
python simulate_attack.py --mode ml --dos 3 --normal 5 --delay 0.5

# ML single-predict mode (POST /predict)
python simulate_attack.py --mode ml --normal 2 --dos 1 --probe 1

python simulate_attack.py --mode ml --use-backend-packets --normal 10 --dos 5 --probe 3 --delay 0.5
# ML batch mode (POST /predict/batch)
python simulate_attack.py --mode ml --batch --normal 2 --dos 1 --probe 1

# Spring backend mode (POST /api/packets)
python simulate_attack.py --mode spring --token "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJodW5nbGV2aTEiLCJpYXQiOjE3ODAxOTU5NjAsImV4cCI6MTc4MDI4MjM2MH0.o3IAXGqIzoChew5NPwTQ4hHvQC1mNeL02nxAxwxRo7fKZt3WnmugoIRiHgmHAQPGqHfUuKh-_AH26WJbrKUjmw" --normal 2 --dos 1 --probe 1

python simulate_attack.py --mode ml --batch --use-backend-packets --normal 10 --dos 5 --probe 3 --delay 0.5
