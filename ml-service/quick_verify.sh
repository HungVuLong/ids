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
$env:TEST_JWT_TOKEN = "VGhpcyBpcyBhIHZlcnkgc2VjdXJlIHNlY3JldCBrZXkgZm9yIEpXVA"; $env:SPRING_URL = "http://backend:1010/api/packets"
python simulate_attack.py --dos 3 --normal 5 --delay 0.5

python simulate_attack.py --token "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJodW5nbGV2aTEiLCJpYXQiOjE3NzgxMzcwODgsImV4cCI6MTc3ODIyMzQ4OH0.bHMafhBuhsHbaHaPPCpOn_arn_dy9xDpwdkU8JSgXu1nXyx2m0qOEEP4LtZim0JstyYonkL2mJsxt61I9satQQ" --normal 10 --dos 5 --probe 3 --delay 0.5
python simulate_attack.py --mode spring --token "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJodW5nbGV2aTEiLCJpYXQiOjE3NzgxMzcwODgsImV4cCI6MTc3ODIyMzQ4OH0.bHMafhBuhsHbaHaPPCpOn_arn_dy9xDpwdkU8JSgXu1nXyx2m0qOEEP4LtZim0JstyYonkL2mJsxt61I9satQQ" --normal 10 --dos 5 --probe 3 --delay 0.5
# ML single-predict mode (POST /predict)
python simulate_attack.py --mode ml --normal 2 --dos 1 --probe 1

python simulate_attack.py --mode ml --use-backend-packets --normal 10 --dos 5 --probe 3 --delay 0.5
# ML batch mode (POST /predict/batch)
python simulate_attack.py --mode ml --batch --normal 2 --dos 1 --probe 1

# Spring backend mode (POST /api/packets)
python simulate_attack.py --mode spring --token "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJodW5nbGV2aTEiLCJpYXQiOjE3NzgxMzcwODgsImV4cCI6MTc3ODIyMzQ4OH0.bHMafhBuhsHbaHaPPCpOn_arn_dy9xDpwdkU8JSgXu1nXyx2m0qOEEP4LtZim0JstyYonkL2mJsxt61I9satQQ" --normal 2 --dos 1 --probe 1

python simulate_attack.py --mode ml --batch --use-backend-packets --normal 10 --dos 5 --probe 3 --delay 0.5
