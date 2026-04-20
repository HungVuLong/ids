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
python simulate_attack.py --token "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJodW5nbGV2aTEiLCJpYXQiOjE3NzY2NDkyMzQsImV4cCI6MTc3NjczNTYzNH0.I0m78M9fzSFBHa46lVWDTCqnYZWW65EcV5RdlbGl9nX8IZLptAtVsx7Qj0wY3Y6dFJzQsITsj4GJmlM08RiIRw" --normal 10 --dos 5 --probe 3 --delay 0.5

|---- Manual cURL test
$token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJodW5nbGV2aTEiLCJpYXQiOjE3NzY2NDkyMzQsImV4cCI6MTc3NjczNTYzNH0.I0m78M9fzSFBHa46lVWDTCqnYZWW65EcV5RdlbGl9nX8IZLptAtVsx7Qj0wY3Y6dFJzQsITsj4GJmlM08RiIRw"
$url = "http://localhost:1010/api/packets"
$headers = @{ Authorization = "Bearer $token"; "Content-Type" = "application/json" }

1..20 | ForEach-Object {
  $body = @{
    sourceIp   = "192.168.1.$(Get-Random -Minimum 2 -Maximum 254)"
    destIp     = "10.0.0.$(Get-Random -Minimum 2 -Maximum 254)"
    protocol   = @("TCP","ICMP") | Get-Random
    size       = Get-Random -Minimum 40 -Maximum 200
    label      = "attack"
    attackType = @("DoS","Probe") | Get-Random
    confidence = [Math]::Round((Get-Random -Minimum 75 -Maximum 99) / 100, 2)
  } | ConvertTo-Json

  Invoke-RestMethod -Uri $url -Method Post -Headers $headers -Body $body
  Start-Sleep -Milliseconds 300
}

