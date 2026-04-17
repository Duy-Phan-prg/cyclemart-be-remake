-- ============================================
-- Priority Packages Feature - Database Setup
-- ============================================

-- Create priority_packages table
CREATE TABLE IF NOT EXISTS priority_packages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    price DECIMAL(12, 2) NOT NULL,
    duration_days INT NOT NULL,
    priority_level INT NOT NULL CHECK (priority_level BETWEEN 1 AND 10),
    display_color VARCHAR(7) NOT NULL,
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

-- Insert sample priority packages
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

-- ============================================
-- Useful Queries for Managing Priority Packages
-- ============================================

-- Query 1: Lấy tất cả bài post có ưu tiên hoạt động, sắp xếp theo mức ưu tiên
-- SELECT
--     bp.id,
--     bp.title,
--     pp.name as priority_package,
--     pp.priority_level,
--     pps.end_date,
--     pps.is_active
-- FROM bike_posts bp
-- JOIN post_priority_subscriptions pps ON bp.id = pps.post_id
-- JOIN priority_packages pp ON pps.package_id = pp.id
-- WHERE pps.is_active = true AND pps.end_date > NOW()
-- ORDER BY pp.priority_level DESC;

-- Query 2: Kiểm tra các đăng ký đã hết hạn
-- SELECT
--     pps.id,
--     bp.title,
--     pp.name,
--     pps.end_date,
--     NOW() as current_time
-- FROM post_priority_subscriptions pps
-- JOIN bike_posts bp ON pps.post_id = bp.id
-- JOIN priority_packages pp ON pps.package_id = pp.id
-- WHERE pps.is_active = true AND pps.end_date <= NOW();

-- Query 3: Cập nhật trạng thái đăng ký hết hạn
-- UPDATE post_priority_subscriptions
-- SET is_active = false
-- WHERE is_active = true AND end_date <= NOW();

-- Query 4: Lấy doanh thu từ gói ưu tiên
-- SELECT
--     pp.name,
--     COUNT(*) as subscription_count,
--     SUM(pp.price) as total_revenue,
--     MAX(pps.created_at) as latest_subscription
-- FROM post_priority_subscriptions pps
-- JOIN priority_packages pp ON pps.package_id = pp.id
-- GROUP BY pp.id, pp.name
-- ORDER BY total_revenue DESC;
