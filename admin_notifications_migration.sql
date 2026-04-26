-- Migration: Create admin_notifications table
CREATE TABLE admin_notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    message TEXT,
    count INT,
    action_url VARCHAR(500),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP NULL
);

-- Index for better performance
CREATE INDEX idx_admin_notifications_is_read ON admin_notifications(is_read);
CREATE INDEX idx_admin_notifications_type ON admin_notifications(type);
CREATE INDEX idx_admin_notifications_created_at ON admin_notifications(created_at DESC);