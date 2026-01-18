-- Create inventory_items table
CREATE TABLE inventory_items (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL UNIQUE,
    product_name VARCHAR(255) NOT NULL,
    available_quantity INT NOT NULL,
    reserved_quantity INT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create reservations table
CREATE TABLE reservations (
    id VARCHAR(36) PRIMARY KEY,
    order_id VARCHAR(36) NOT NULL,
    product_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

-- Create indexes for better query performance
CREATE INDEX idx_reservations_order_id ON reservations(order_id);
CREATE INDEX idx_reservations_product_id ON reservations(product_id);
CREATE INDEX idx_reservations_order_product ON reservations(order_id, product_id);

-- Insert sample inventory data
INSERT INTO inventory_items (id, product_id, product_name, available_quantity, reserved_quantity, version)
VALUES 
  ('inv-1', 'prod-1', 'Organic Wheat (100kg)', 1000, 0, 0),
  ('inv-2', 'prod-2', 'Rice Seeds (50kg)', 500, 0, 0),
  ('inv-3', 'prod-3', 'Fertilizer NPK (25kg)', 750, 0, 0);
