-- Order table schema for orderdb PostgreSQL database
-- Run this against: jdbc:postgresql://localhost:5432/orderdb

CREATE TABLE IF NOT EXISTS "order" (
    id            SERIAL PRIMARY KEY,
    customer_name  VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    product_name   VARCHAR(255) NOT NULL,
    quantity       INTEGER      NOT NULL CHECK (quantity > 0),
    total_price    NUMERIC(10, 2) NOT NULL CHECK (total_price >= 0),
    status         VARCHAR(50)  NOT NULL DEFAULT 'pending'
                       CHECK (status IN ('pending','processing','shipped','delivered','cancelled')),
    created_at     TIMESTAMP    NOT NULL DEFAULT NOW()
);
