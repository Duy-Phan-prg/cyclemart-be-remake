# Fix Database Schema - Priority Packages

## ⚠️ Vấn đề

Database hiện tại vẫn có cột `display_color` nhưng code đã xóa nó. Khi insert dữ liệu, lỗi:
```
Field 'display_color' doesn't have a default value
```

## ✅ Giải Pháp

### Option 1: Sửa schema hiện tại (Khuyến nghị - Giữ dữ liệu)

Chạy lệnh SQL này:
```sql
-- Xóa cột display_color
ALTER TABLE priority_packages DROP COLUMN display_color;

-- Đổi priority_level từ INT sang VARCHAR
ALTER TABLE priority_packages MODIFY priority_level VARCHAR(20) NOT NULL;

-- Xóa CHECK constraint
ALTER TABLE priority_packages DROP CHECK IF EXISTS priority_packages_chk_1;
```

**Lệnh một lần:**
```bash
mysql -u [user] -p [database] -e "
ALTER TABLE priority_packages DROP COLUMN display_color;
ALTER TABLE priority_packages MODIFY priority_level VARCHAR(20) NOT NULL;
"
```

### Option 2: Tái tạo hoàn toàn (Xóa toàn bộ dữ liệu)

```bash
# Chạy file fix_priority_packages_schema.sql 
# (phần được comment giữa /* ... */)

mysql -u [user] -p [database] < fix_priority_packages_schema.sql
```

**Cảnh báo**: Lựa chọn này sẽ **xóa tất cả dữ liệu** trong bảng

---

## 🔧 Cách thực hiện

### Cách 1: Sử dụng MySQL Command Line

```bash
# Kết nối vào database
mysql -u root -p

# Chọn database
USE [your_database_name];

# Xóa cột display_color
ALTER TABLE priority_packages DROP COLUMN display_color;

# Sửa kiểu priority_level
ALTER TABLE priority_packages MODIFY priority_level VARCHAR(20) NOT NULL;

# Kiểm tra schema
DESC priority_packages;

# Thoát
EXIT;
```

### Cách 2: Sử dụng phần mềm GUI (MySQL Workbench, etc.)

1. Mở phần mềm quản lý database
2. Kết nối vào database
3. Right-click table `priority_packages` → Edit table
4. Xóa cột `display_color`
5. Sửa cột `priority_level`: type = VARCHAR(20)
6. Apply changes

### Cách 3: Chạy SQL file

```bash
# Copy file vào thư mục MySQL hoặc chạy trực tiếp
mysql -u root -p your_database < fix_priority_packages_schema.sql
```

---

## ✅ Kiểm tra sau khi fix

```sql
-- Xem schema
DESC priority_packages;

-- Kết quả mong đợi:
-- Field              | Type        | Null | Key
-- id                 | bigint      | NO   | PRI
-- name               | varchar(100)| NO   | UNI
-- description        | text        | YES  |
-- price              | decimal     | NO   |
-- duration_days      | int         | NO   |
-- priority_level     | varchar(20) | NO   |
-- is_active          | boolean     | NO   |
-- created_at         | timestamp   | NO   |
-- updated_at         | timestamp   | NO   |
```

---

## 📝 Sau khi fix xong

### 1. Insert sample data

```sql
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
    'Ưu tiên đầu tiên trong mục category',
    100000,
    14,
    'PLATINUM',
    true
);
```

### 2. Test lại API

```bash
curl -X POST http://localhost:8080/api/v1/priority-packages \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer [your_token]" \
  -d '{
    "name": "Gói Platinum",
    "description": "Ưu tiên đầu tiên trong mục category",
    "price": 100000,
    "durationDays": 7,
    "priorityLevel": "PLATINUM",
    "isActive": true
  }'
```

---

## 🐛 Nếu vẫn bị lỗi

### Lỗi 1: Table doesn't exist
```sql
-- Kiểm tra table
SHOW TABLES;
SHOW TABLES LIKE 'priority%';
```

### Lỗi 2: Column doesn't exist
```sql
-- Kiểm tra columns
DESC priority_packages;
SHOW COLUMNS FROM priority_packages;
```

### Lỗi 3: Foreign key constraint fails
```sql
-- Kiểm tra foreign keys
SHOW CREATE TABLE post_priority_subscriptions;
```

---

## 📚 Files liên quan

- `fix_priority_packages_schema.sql` - Script fix schema
- `priority_packages_migration.sql` - Script tạo bảng (cũ, cần update)
- `PriorityPackageServiceImpl.java` - Code Java

---

## ✨ Tóm tắt

1. **Xóa cột `display_color`** từ bảng
2. **Đổi `priority_level`** từ `INT` sang `VARCHAR(20)`
3. **Insert sample data** mới
4. **Test API** lại

**Thời gian**: ~5 phút

**Khó độ**: ⭐ (rất dễ)

---

Done! 🎉 Sau khi fix, API sẽ hoạt động bình thường.
