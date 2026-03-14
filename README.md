<div align="center">

<img src="https://img.shields.io/badge/Java-17-orange?style=for-the-badge&logo=openjdk&logoColor=white"/>
<img src="https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
<img src="https://img.shields.io/badge/Python-3.10-3776AB?style=for-the-badge&logo=python&logoColor=white"/>
<img src="https://img.shields.io/badge/Flask-3.x-000000?style=for-the-badge&logo=flask&logoColor=white"/>
<img src="https://img.shields.io/badge/React-18-61DAFB?style=for-the-badge&logo=react&logoColor=black"/>
<img src="https://img.shields.io/badge/PostgreSQL-15-4169E1?style=for-the-badge&logo=postgresql&logoColor=white"/>

# 🔒 NetSentinel IDS

### AI-Powered Network Intrusion Detection System

*Hệ thống phát hiện xâm nhập mạng thông minh ứng dụng Machine Learning*

[![Build](https://img.shields.io/badge/build-passing-brightgreen?style=flat-square)]()
[![ML Accuracy](https://img.shields.io/badge/ML_Accuracy-≥90%25-brightgreen?style=flat-square)]()
[![License](https://img.shields.io/badge/license-MIT-blue?style=flat-square)]()
[![Sprints](https://img.shields.io/badge/sprints-4_×_1_week-orange?style=flat-square)]()

[📖 Tổng Quan](#-tổng-quan) • [🏗️ Kiến Trúc](#️-kiến-trúc) • [🚀 Quick Start](#-quick-start) • [📡 API](#-api-reference) • [🧪 Testing](#-testing) • [👥 Team](#-team)

---

</div>

## 📖 Tổng Quan

**NetSentinel** là hệ thống phát hiện xâm nhập mạng (IDS) sử dụng Machine Learning để phân tích lưu lượng mạng theo thời gian thực, tự động phân loại hành vi bất thường và cảnh báo người quản trị qua dashboard trực quan.

Khác với các giải pháp rule-based truyền thống (Snort, Suricata), NetSentinel ứng dụng mô hình **Random Forest / XGBoost** huấn luyện trên dataset **NSL-KDD** để phát hiện các kiểu tấn công chưa được định nghĩa trước.

### ✨ Tính Năng Chính

| Tính năng | Mô tả |
|-----------|-------|
| 🤖 **ML Detection** | Phân loại traffic real-time với accuracy ≥ 90% |
| ⚡ **Real-time Alerts** | WebSocket push alert đến dashboard trong ≤ 1 giây |
| 📊 **Dashboard** | Biểu đồ thống kê, timeline, phân loại theo attack type |
| 🔐 **Authentication** | JWT-based auth, role ADMIN / VIEWER |
| 📄 **Report Export** | Xuất báo cáo PDF theo khoảng thời gian |
| 🎯 **4 Attack Types** | DoS · Probe · R2L · U2R |

---

## 🏗️ Kiến Trúc

```
┌─────────────────────────────────────────────────────────────┐
│                     React.js Frontend                        │
│          Dashboard · Alerts · Statistics · Settings          │
└──────────────────────┬──────────────────────────────────────┘
                       │ HTTP / WebSocket (STOMP)
┌──────────────────────▼──────────────────────────────────────┐
│                  Spring Boot Backend                          │
│   REST API · JWT Auth · WebSocket Server · Alert Engine      │
└──────┬───────────────────────────────────┬───────────────────┘
       │ JPA                               │ WebClient (HTTP)
┌──────▼──────┐                   ┌────────▼────────┐
│ PostgreSQL  │                   │ Python ML       │
│   ids_db    │                   │ Flask + sklearn  │
│             │                   │ /predict        │
└─────────────┘                   └─────────────────┘
```

### 🗂️ Cấu Trúc Project

```
netsentinel-ids/
├── 📁 backend/                  # Spring Boot 3.2 — Java 17
│   ├── src/main/java/com/ids/
│   │   ├── config/              # Security, WebSocket, WebClient config
│   │   ├── controller/          # REST controllers
│   │   ├── service/             # Business logic
│   │   ├── repository/          # JPA repositories
│   │   ├── entity/              # JPA entities
│   │   ├── dto/                 # Request / Response DTOs
│   │   ├── security/            # JWT filter, UserDetailsService
│   │   ├── websocket/           # STOMP alert handler
│   │   └── exception/           # Global exception handler
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/        # Flyway SQL migrations
│   └── pom.xml
│
├── 📁 ml-service/               # Python 3.10 — Flask ML Service
│   ├── app.py                   # Flask entry point
│   ├── config.py                # Constants, paths, hyperparams
│   ├── preprocess.py            # NSL-KDD preprocessing pipeline
│   ├── train.py                 # Model training & evaluation
│   ├── predict.py               # Inference engine (stateless)
│   ├── utils/
│   │   ├── feature_builder.py   # JSON → numpy feature vector
│   │   └── metrics.py           # Evaluation helpers
│   ├── artifacts/               # Saved model, scaler, encoders (.pkl)
│   ├── data/
│   │   ├── raw/                 # NSL-KDD original files
│   │   └── processed/           # Preprocessed .npy arrays
│   ├── tests/                   # pytest test suite
│   ├── simulate_attack.py       # End-to-end test helper
│   └── requirements.txt
│
├── 📁 frontend/                 # React 18 — Vite + Tailwind CSS
│   ├── src/
│   │   ├── pages/               # LiveMonitor, Alerts, Statistics, Settings
│   │   ├── components/          # Reusable UI components
│   │   ├── hooks/               # useWebSocket, useAuth, useAlerts
│   │   ├── services/            # Axios API calls
│   │   └── context/             # AuthContext
│   └── package.json
│
└── 📄 README.md
```

---

## ⚙️ Prerequisites

Đảm bảo đã cài đặt đầy đủ trước khi chạy:

| Tool | Version | Check |
|------|---------|-------|
| Java JDK | 17+ | `java -version` |
| Maven | 3.8+ | `mvn -version` |
| Python | 3.10+ | `python --version` |
| Node.js | 18+ | `node --version` |
| PostgreSQL | 15+ | `psql --version` |

---

## 🚀 Quick Start

### Bước 1 — Clone Repository

```bash
git clone https://github.com/your-username/netsentinel-ids.git
cd netsentinel-ids
```

### Bước 2 — Setup Database

```sql
-- Chạy trong psql hoặc pgAdmin
CREATE DATABASE ids_db;
CREATE USER ids_user WITH PASSWORD 'your_password';
GRANT ALL PRIVILEGES ON DATABASE ids_db TO ids_user;
```

### Bước 3 — Cấu Hình Environment Variables

Tạo file `backend/src/main/resources/application-local.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/ids_db
    username: ids_user
    password: your_password

jwt:
  secret: your-super-secret-key-min-32-chars
  expiration: 86400000   # 24 hours in ms

ml:
  service:
    url: http://localhost:5000
```

Hoặc export environment variables:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/ids_db
export DB_USERNAME=ids_user
export DB_PASSWORD=your_password
export JWT_SECRET=your-super-secret-key-min-32-chars
export ML_SERVICE_URL=http://localhost:5000
```

### Bước 4 — Setup & Chạy Python ML Service

```bash
cd ml-service

# Tạo virtual environment
python -m venv venv
source venv/bin/activate        # Linux/Mac
# venv\Scripts\activate         # Windows

# Cài dependencies
pip install -r requirements.txt

# Download NSL-KDD dataset vào data/raw/
# Link: https://www.kaggle.com/datasets/hassan06/nslkdd

# Tiền xử lý dữ liệu (chạy 1 lần)
python preprocess.py

# Train model (chạy 1 lần, ~2-5 phút)
python train.py

# Khởi động Flask service
python app.py
# ✅ ML Service running on http://localhost:5000
```

### Bước 5 — Chạy Spring Boot Backend

```bash
cd backend

# Build và chạy
mvn spring-boot:run -Dspring-boot.run.profiles=local

# Hoặc build JAR trước
mvn clean package -DskipTests
java -jar target/ids-backend-*.jar --spring.profiles.active=local

# ✅ Backend running on http://localhost:8080
```

### Bước 6 — Chạy React Frontend

```bash
cd frontend

# Cài dependencies
npm install

# Tạo file .env
echo "VITE_API_URL=http://localhost:8080" > .env
echo "VITE_WS_URL=ws://localhost:8080/ws" >> .env

# Chạy dev server
npm run dev

# ✅ Frontend running on http://localhost:5173
```

### ✅ Kiểm Tra Hệ Thống

```bash
# 1. ML Service health
curl http://localhost:5000/health
# Expected: {"status":"ok","model_loaded":true}

# 2. Backend health
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}

# 3. Register tài khoản đầu tiên
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@ids.local","password":"Admin@123"}'

# 4. Login lấy JWT token
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}'
```

---

## 📡 API Reference

**Base URL:** `http://localhost:8080/api`

**Authentication:** Tất cả endpoints (trừ `/auth/**`) yêu cầu header:
```
Authorization: Bearer <jwt_token>
```

### 🔐 Auth

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| `POST` | `/auth/register` | ❌ | Đăng ký tài khoản |
| `POST` | `/auth/login` | ❌ | Đăng nhập, nhận JWT |
| `GET` | `/auth/me` | ✅ | Thông tin user hiện tại |

### 📦 Packets

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| `POST` | `/packets` | ✅ | Gửi packet để phân tích |
| `GET` | `/packets?page=0&size=20&label=normal` | ✅ | Lấy danh sách packets |
| `GET` | `/packets/{id}` | ✅ | Chi tiết 1 packet |
| `DELETE` | `/packets/{id}` | ✅ ADMIN | Xoá packet |

**POST /packets — Request body:**
```json
{
  "srcIp": "192.168.1.100",
  "dstIp": "10.0.0.1",
  "srcPort": 12345,
  "dstPort": 80,
  "protocol": "tcp",
  "duration": 0,
  "land": 0,
  "wrongFragment": 0,
  "urgent": 0
}
```

### 🚨 Alerts

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| `GET` | `/alerts?page=0&size=20&status=OPEN&attackType=DoS` | ✅ | Lấy danh sách alerts |
| `GET` | `/alerts/{id}` | ✅ | Chi tiết 1 alert |
| `PATCH` | `/alerts/{id}/status` | ✅ | Cập nhật trạng thái |
| `DELETE` | `/alerts/{id}` | ✅ ADMIN | Xoá alert |

**PATCH /alerts/{id}/status — Request body:**
```json
{ "status": "RESOLVED" }
```

### 📊 Dashboard

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| `GET` | `/dashboard/stats` | ✅ | Thống kê tổng quan |
| `GET` | `/dashboard/stats?from=2026-03-01&to=2026-03-31` | ✅ | Thống kê theo khoảng thời gian |

**GET /dashboard/stats — Response:**
```json
{
  "totalPackets": 1520,
  "totalAlerts": 87,
  "openAlerts": 12,
  "resolvedAlerts": 75,
  "alertsByType": { "DoS": 45, "Probe": 30, "R2L": 8, "U2R": 4 },
  "alertsBySeverity": { "CRITICAL": 10, "HIGH": 32, "MEDIUM": 45 },
  "alertTimeline": [
    { "date": "2026-03-13", "count": 5 }
  ],
  "systemStatus": "NORMAL"
}
```

### 📄 Reports

| Method | Endpoint | Auth | Mô tả |
|--------|----------|------|-------|
| `POST` | `/reports` | ✅ ADMIN | Tạo báo cáo |
| `GET` | `/reports?page=0&size=10` | ✅ | Danh sách báo cáo |
| `GET` | `/reports/{id}` | ✅ | Chi tiết báo cáo |
| `DELETE` | `/reports/{id}` | ✅ ADMIN | Xoá báo cáo |

### 🤖 ML Service (Internal)

| Method | Endpoint | Mô tả |
|--------|----------|-------|
| `GET` | `/health` | Kiểm tra trạng thái service |
| `POST` | `/predict` | Phân loại 1 packet |
| `POST` | `/predict/batch` | Phân loại nhiều packets (max 100) |

**POST /predict — Request:**
```json
{
  "features": {
    "duration": 0,
    "protocol_type": "tcp",
    "service": "http",
    "flag": "SF"
  }
}
```

**POST /predict — Response:**
```json
{
  "label": "attack",
  "attack_type": "DoS",
  "confidence": 0.97,
  "prediction_time_ms": 8
}
```

### 🔌 WebSocket

**Endpoint:** `ws://localhost:8080/ws` (SockJS fallback)

**Subscribe topic:** `/topic/alerts`

```javascript
import { Client } from '@stomp/stompjs';

const client = new Client({ brokerURL: 'ws://localhost:8080/ws' });
client.onConnect = () => {
  client.subscribe('/topic/alerts', (message) => {
    const alert = JSON.parse(message.body);
    console.log('New alert:', alert.attackType, alert.severity);
  });
};
client.activate();
```

---

## 🧪 Testing

### Backend Unit Tests

```bash
cd backend

# Chạy tất cả tests
mvn test

# Chạy với coverage report
mvn test jacoco:report
# Report: target/site/jacoco/index.html

# Chạy 1 class cụ thể
mvn test -Dtest=AlertServiceImplTest
```

### ML Service Tests

```bash
cd ml-service
source venv/bin/activate

# Chạy tất cả tests
pytest tests/ -v

# Với coverage
pytest tests/ -v --cov=. --cov-report=html
# Report: htmlcov/index.html

# Chạy 1 file
pytest tests/test_predict.py -v
```

### End-to-End Test (Simulate Attack)

```bash
# Đảm bảo cả 3 services đang chạy

# Lấy JWT token
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"Admin@123"}' | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

# Chạy simulation
cd ml-service
python simulate_attack.py \
  --token $TOKEN \
  --normal 10 \
  --dos 5 \
  --probe 3 \
  --delay 0.5

# Kiểm tra alerts được tạo
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/alerts?status=OPEN
```

---

## 🌱 Environment Variables

### Backend (`application.yml`)

| Variable | Default | Mô tả |
|----------|---------|-------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/ids_db` | PostgreSQL connection URL |
| `DB_USERNAME` | `ids_user` | DB username |
| `DB_PASSWORD` | *(required)* | DB password |
| `JWT_SECRET` | *(required, min 32 chars)* | JWT signing secret |
| `JWT_EXPIRATION` | `86400000` | Token TTL (ms) |
| `ML_SERVICE_URL` | `http://localhost:5000` | Python ML service URL |
| `SERVER_PORT` | `8080` | Backend port |

### ML Service (`config.py` / env)

| Variable | Default | Mô tả |
|----------|---------|-------|
| `FLASK_HOST` | `0.0.0.0` | Flask bind host |
| `FLASK_PORT` | `5000` | Flask port |
| `ML_SERVICE_URL` | `http://localhost:5000` | Self URL (for health checks) |

### Frontend (`.env`)

| Variable | Default | Mô tả |
|----------|---------|-------|
| `VITE_API_URL` | `http://localhost:8080` | Backend base URL |
| `VITE_WS_URL` | `ws://localhost:8080/ws` | WebSocket URL |

---

## 📊 ML Model Info

| Thông tin | Giá trị |
|-----------|---------|
| **Dataset** | NSL-KDD (Train+: 125,973 records) |
| **Features** | 41 NSL-KDD features |
| **Task** | Binary classification (normal vs attack) |
| **Models trained** | Random Forest, XGBoost |
| **Selection metric** | F1-Score |
| **Target accuracy** | ≥ 90% |
| **Attack types** | DoS · Probe · R2L · U2R |
| **Inference latency** | ≤ 500ms per packet |

### Attack Types

| Type | Mô tả | Ví dụ |
|------|-------|-------|
| **DoS** | Denial of Service — làm quá tải hệ thống | neptune, smurf, pod |
| **Probe** | Quét cổng, thu thập thông tin mạng | ipsweep, portsweep, nmap |
| **R2L** | Remote to Local — truy cập trái phép | ftp_write, guess_passwd |
| **U2R** | User to Root — leo thang đặc quyền | buffer_overflow, rootkit |

---

## 👥 Team

| Thành viên             | Role | Trách nhiệm |
|------------------------|------|-------------|
| **Hùng Vũ Long**       | Backend Lead | Java Spring Boot, Python ML Service, PostgreSQL |
| **Nguyễn Thanh Triều** | Frontend Lead | React.js, Dashboard, WebSocket UI |
| **Lương Thế Nguyên**   | BA / Tech Writer | Tài liệu, Test Cases, Báo cáo, Slide |

---

## 📅 Timeline

```
Sprint 1  13/03 – 19/03   Setup + Thiết kế + Chuẩn bị dữ liệu
Sprint 2  20/03 – 26/03   Core APIs + ML model + UI skeleton
Sprint 3  27/03 – 02/04   End-to-end: detect attack real-time
Sprint 4  03/04 – 09/04   Polish + Testing + Báo cáo hoàn chỉnh
```

---

## 🛠️ Tech Stack

### Backend
- **Java 17** + **Spring Boot 3.2**
- **Spring Security 6** + **JWT** (jjwt 0.12)
- **Spring Data JPA** + **Flyway** migration
- **WebSocket** (STOMP) + **WebClient** (reactive)
- **PostgreSQL 15**

### ML Service
- **Python 3.10** + **Flask 3.x**
- **scikit-learn** (Random Forest) + **XGBoost**
- **pandas** + **numpy** + **joblib**
- Dataset: **NSL-KDD**

### Frontend
- **React 18** + **Vite**
- **Tailwind CSS**
- **@stomp/stompjs** (WebSocket)
- **Recharts** (charts)
- **Axios**

---

## 📄 License

This project is licensed under the MIT License.

---

<div align="center">

**NetSentinel IDS** 

</div>