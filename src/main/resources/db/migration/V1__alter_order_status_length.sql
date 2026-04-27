-- Tăng độ dài cột order_status để chứa PENDING_SELLER_CONFIRMATION (27 ký tự)
ALTER TABLE payments MODIFY COLUMN order_status VARCHAR(50);
