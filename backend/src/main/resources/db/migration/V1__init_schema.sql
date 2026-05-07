-- Create user
CREATE USER ids_user WITH PASSWORD 'Hungvulong1@';

-- Create database
CREATE DATABASE ids_db;

-- Grant ownership
ALTER DATABASE ids_db OWNER TO ids_user;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE ids_db TO ids_user;
-- users table (PostgreSQL syntax)
CREATE TABLE users (
                       id         BIGSERIAL PRIMARY KEY,
                       username   VARCHAR(50)  UNIQUE NOT NULL,
                       email      VARCHAR(100) UNIQUE NOT NULL,
                       password   VARCHAR(255) NOT NULL,
                       role       VARCHAR(20)  NOT NULL DEFAULT 'VIEWER',
                       created_at TIMESTAMP    NOT NULL DEFAULT NOW()
);
-- Tab: Copilot generates packets, alerts, reports tables following this pattern

CREATE TABLE packets (
                        id         BIGSERIAL PRIMARY KEY,
                        source_ip  VARCHAR(45),
                        dest_ip    VARCHAR(45),
                        src_port   INTEGER,
                        dst_port   INTEGER,
                        protocol   VARCHAR(10) NOT NULL,
                        duration   DOUBLE PRECISION NOT NULL DEFAULT 0,
                        land       INTEGER NOT NULL DEFAULT 0,
                        wrong_fragment INTEGER NOT NULL DEFAULT 0,
                        urgent     INTEGER NOT NULL DEFAULT 0,
                        label      VARCHAR(100) NOT NULL,
                        attack_type VARCHAR(50),
                        confidence DOUBLE PRECISION NOT NULL,
                        captured_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE alerts (
                        id         BIGSERIAL PRIMARY KEY,
                        packet_id  BIGINT       NOT NULL,
                        alert_type VARCHAR(50)  NOT NULL,
                        severity   VARCHAR(20)  NOT NULL,
                        message    TEXT         NOT NULL,
                        status     VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
                        resolved_by BIGINT,
                        resolved_at TIMESTAMP,
                        timestamp  TIMESTAMP    NOT NULL DEFAULT NOW(),
                        updated_at TIMESTAMP,
                        FOREIGN KEY (packet_id) REFERENCES packets(id) ON DELETE CASCADE,
                        FOREIGN KEY (resolved_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE reports (
                        id         BIGSERIAL PRIMARY KEY,
                        title      VARCHAR(100) NOT NULL,
                        content    TEXT         NOT NULL,
                        generated_by BIGINT     NOT NULL,
                        created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
                        FOREIGN KEY (generated_by) REFERENCES users(id) ON DELETE CASCADE
);
