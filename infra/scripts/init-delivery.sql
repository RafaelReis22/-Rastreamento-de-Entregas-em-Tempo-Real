-- ============================================================
-- Schema: delivery_db
-- ============================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TYPE delivery_status AS ENUM (
    'PENDING',
    'ACCEPTED',
    'PICKED_UP',
    'IN_TRANSIT',
    'DELIVERED',
    'CANCELLED',
    'FAILED'
);

CREATE TABLE IF NOT EXISTS delivery_orders (
    id               UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id      UUID            NOT NULL,
    deliverer_id     UUID,
    status           delivery_status NOT NULL DEFAULT 'PENDING',
    origin_address   TEXT            NOT NULL,
    dest_address     TEXT            NOT NULL,
    notes            TEXT,
    created_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP       NOT NULL DEFAULT NOW(),
    accepted_at      TIMESTAMP,
    delivered_at     TIMESTAMP,
    cancelled_at     TIMESTAMP
);

CREATE TABLE IF NOT EXISTS order_status_history (
    id          BIGSERIAL        PRIMARY KEY,
    order_id    UUID             NOT NULL REFERENCES delivery_orders(id) ON DELETE CASCADE,
    old_status  delivery_status,
    new_status  delivery_status  NOT NULL,
    changed_by  UUID             NOT NULL,
    changed_at  TIMESTAMP        NOT NULL DEFAULT NOW(),
    notes       TEXT
);

CREATE INDEX IF NOT EXISTS idx_orders_customer_id   ON delivery_orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_deliverer_id  ON delivery_orders(deliverer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status        ON delivery_orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at    ON delivery_orders(created_at);
CREATE INDEX IF NOT EXISTS idx_status_history_order ON order_status_history(order_id);
