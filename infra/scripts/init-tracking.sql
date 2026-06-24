-- ============================================================
-- Schema: tracking_db
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS delivery_orders (
    id               UUID      PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id      UUID      NOT NULL,
    deliverer_id     UUID,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    last_lat         DECIMAL(10,8),
    last_lng         DECIMAL(11,8),
    last_location_at TIMESTAMP,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Particionada mensalmente (LGPD: retenção 90 dias)
CREATE TABLE IF NOT EXISTS location_history (
    id            BIGSERIAL,
    order_id      UUID          NOT NULL,
    deliverer_id  UUID          NOT NULL,
    lat           DECIMAL(10,8) NOT NULL,
    lng           DECIMAL(11,8) NOT NULL,
    accuracy_m    DECIMAL(6,2),
    is_suspicious BOOLEAN       NOT NULL DEFAULT FALSE,
    recorded_at   TIMESTAMP     NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, recorded_at)
) PARTITION BY RANGE (recorded_at);

CREATE TABLE IF NOT EXISTS location_history_2025_06 PARTITION OF location_history
    FOR VALUES FROM ('2025-06-01') TO ('2025-07-01');
CREATE TABLE IF NOT EXISTS location_history_2025_07 PARTITION OF location_history
    FOR VALUES FROM ('2025-07-01') TO ('2025-08-01');
CREATE TABLE IF NOT EXISTS location_history_2025_08 PARTITION OF location_history
    FOR VALUES FROM ('2025-08-01') TO ('2025-09-01');
CREATE TABLE IF NOT EXISTS location_history_2025_09 PARTITION OF location_history
    FOR VALUES FROM ('2025-09-01') TO ('2025-10-01');
CREATE TABLE IF NOT EXISTS location_history_2025_10 PARTITION OF location_history
    FOR VALUES FROM ('2025-10-01') TO ('2025-11-01');
CREATE TABLE IF NOT EXISTS location_history_2025_11 PARTITION OF location_history
    FOR VALUES FROM ('2025-11-01') TO ('2025-12-01');
CREATE TABLE IF NOT EXISTS location_history_2025_12 PARTITION OF location_history
    FOR VALUES FROM ('2025-12-01') TO ('2026-01-01');
CREATE TABLE IF NOT EXISTS location_history_2026_01 PARTITION OF location_history
    FOR VALUES FROM ('2026-01-01') TO ('2026-02-01');
CREATE TABLE IF NOT EXISTS location_history_2026_02 PARTITION OF location_history
    FOR VALUES FROM ('2026-02-01') TO ('2026-03-01');
CREATE TABLE IF NOT EXISTS location_history_2026_03 PARTITION OF location_history
    FOR VALUES FROM ('2026-03-01') TO ('2026-04-01');
CREATE TABLE IF NOT EXISTS location_history_2026_04 PARTITION OF location_history
    FOR VALUES FROM ('2026-04-01') TO ('2026-05-01');
CREATE TABLE IF NOT EXISTS location_history_2026_05 PARTITION OF location_history
    FOR VALUES FROM ('2026-05-01') TO ('2026-06-01');
CREATE TABLE IF NOT EXISTS location_history_2026_06 PARTITION OF location_history
    FOR VALUES FROM ('2026-06-01') TO ('2026-07-01');
CREATE TABLE IF NOT EXISTS location_history_2026_07 PARTITION OF location_history
    FOR VALUES FROM ('2026-07-01') TO ('2026-08-01');
CREATE TABLE IF NOT EXISTS location_history_2026_08 PARTITION OF location_history
    FOR VALUES FROM ('2026-08-01') TO ('2026-09-01');
CREATE TABLE IF NOT EXISTS location_history_2026_09 PARTITION OF location_history
    FOR VALUES FROM ('2026-09-01') TO ('2026-10-01');
CREATE TABLE IF NOT EXISTS location_history_2026_10 PARTITION OF location_history
    FOR VALUES FROM ('2026-10-01') TO ('2026-11-01');
CREATE TABLE IF NOT EXISTS location_history_2026_11 PARTITION OF location_history
    FOR VALUES FROM ('2026-11-01') TO ('2026-12-01');
CREATE TABLE IF NOT EXISTS location_history_2026_12 PARTITION OF location_history
    FOR VALUES FROM ('2026-12-01') TO ('2027-01-01');

CREATE INDEX IF NOT EXISTS idx_location_order_id    ON location_history(order_id);
CREATE INDEX IF NOT EXISTS idx_location_recorded_at ON location_history(recorded_at);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id   ON delivery_orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_deliverer_id  ON delivery_orders(deliverer_id);
