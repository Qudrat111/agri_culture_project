CREATE TABLE procurement_sagas (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    buyer_id VARCHAR(36),
    total_amount DECIMAL(19, 2),
    status VARCHAR(20) NOT NULL,
    current_step VARCHAR(30) NOT NULL,
    inventory_reserved BOOLEAN DEFAULT FALSE,
    payment_processed BOOLEAN DEFAULT FALSE,
    failure_reason TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX idx_procurement_sagas_order_id ON procurement_sagas(order_id);
