-- Procurement Orders table
CREATE TABLE procurement_orders (
    id VARCHAR(36) PRIMARY KEY,
    buyer_id VARCHAR(36) NOT NULL,
    supplier_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED'))
);

-- Order Items table (embedded value objects)
CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity DECIMAL(10, 2) NOT NULL,
    price DECIMAL(15, 2) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES procurement_orders(id) ON DELETE CASCADE
);

-- Outbox Events table (for transactional outbox pattern)
CREATE TABLE outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(36) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);

-- Idempotency Keys table
CREATE TABLE idempotency_keys (
    key VARCHAR(100) PRIMARY KEY,
    response TEXT,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

-- Indexes for performance
CREATE INDEX idx_orders_buyer_id ON procurement_orders(buyer_id);
CREATE INDEX idx_orders_supplier_id ON procurement_orders(supplier_id);
CREATE INDEX idx_orders_status ON procurement_orders(status);
CREATE INDEX idx_orders_created_at ON procurement_orders(created_at);

CREATE INDEX idx_order_items_order_id ON order_items(order_id);
CREATE INDEX idx_order_items_product_id ON order_items(product_id);

CREATE INDEX idx_outbox_processed_created ON outbox_events(processed, created_at) WHERE processed = FALSE;
CREATE INDEX idx_outbox_aggregate ON outbox_events(aggregate_type, aggregate_id);

CREATE INDEX idx_idempotency_expires ON idempotency_keys(expires_at);
