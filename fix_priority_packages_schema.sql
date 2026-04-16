-- ============================================
-- Fix Priority Packages Table Schema
-- ============================================

-- Drop old table if exists (WARNING: This will delete all data)
-- DROP TABLE IF EXISTS post_priority_subscriptions;
-- DROP TABLE IF EXISTS priority_packages;

-- OR if tables exist, just fix the schema:

-- 1. Remove display_color column from priority_packages
ALTER TABLE priority_packages DROP COLUMN display_color;

-- 2. Modify priority_level to VARCHAR (for enum)
ALTER TABLE priority_packages MODIFY priority_level VARCHAR(20) NOT NULL;

-- 3. Remove the CHECK constraint (if it exists)
ALTER TABLE priority_packages DROP CHECK IF EXISTS priority_packages_chk_1;

-- ============================================
-- OR if you want to DROP and RECREATE completely:
-- ============================================

/*
-- Drop tables (WARNING: deletes all data)
DROP TABLE IF EXISTS post_priority_subscriptions;
DROP TABLE IF EXISTS priority_packages;

-- Create priority_packages table (NEW SCHEMA)
CREATE TABLE IF NOT EXISTS priority_packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    duration_days INT NOT NULL,
    priority_level VARCHAR(20) NOT NULL,
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_is_active (is_active),
    INDEX idx_priority_level (priority_level)
);

-- Create post_priority_subscriptions table
CREATE TABLE IF NOT EXISTS post_priority_subscriptions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id BIGINT NOT NULL,
    package_id BIGINT NOT NULL,
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP NOT NULL,
    is_active BOOLEAN DEFAULT true NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (post_id) REFERENCES bike_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (package_id) REFERENCES priority_packages(id) ON DELETE CASCADE,
    INDEX idx_post_id (post_id),
    INDEX idx_package_id (package_id),
    INDEX idx_is_active (is_active),
    INDEX idx_end_date (end_date),
    UNIQUE KEY unique_active_subscription (post_id, package_id, is_active)
);

-- Insert sample priority packages (NEW DATA)
INSERT INTO priority_packages (name, description, price, duration_days, priority_level, is_active)
VALUES
(
    'Gói Silver',
    'Hiển thị bài đăng với badge màu bạc',
    25000,
    3,
    'SILVER',
    true
),
(
    'Gói Gold',
    'Hiển thị nổi bật trên trang chủ',
    50000,
    7,
    'GOLD',
    true
),
(
    'Gói Platinum',
    'Ưu tiên đầu tiên trong mục category, hiển thị nổi bật nhất',
    100000,
    14,
    'PLATINUM',
    true
);
*/
