# Quick Database Fix

## ⚡ Cách fix nhanh nhất (chỉ 1 dòng)

### Sử dụng MySQL Command Line

```bash
mysql -u root -p your_database_name -e "ALTER TABLE priority_packages DROP COLUMN display_color; ALTER TABLE priority_packages MODIFY priority_level VARCHAR(20) NOT NULL;"
```

### Hoặc sử dụng file SQL

1. Mở MySQL Workbench hoặc MySQL command line

2. Chạy:
```sql
ALTER TABLE priority_packages DROP COLUMN display_color;
ALTER TABLE priority_packages MODIFY priority_level VARCHAR(20) NOT NULL;
```

3. Kiểm tra:
```sql
DESC priority_packages;
```

---

## ✅ Sau khi fix

**Test API ngay bằng curl:**

```bash
curl -X 'POST' \
  'http://localhost:8080/api/v1/priority-packages' \
  -H 'accept: */*' \
  -H 'Authorization: Bearer [YOUR_TOKEN]' \
  -H 'Content-Type: application/json' \
  -d '{
    "name": "Gói Platinum",
    "description": "Ưu tiên đầu tiên trong mục category",
    "price": 100000,
    "durationDays": 7,
    "priorityLevel": "PLATINUM",
    "isActive": true
  }'
```

**Expected Response**: 201 Created

---

Done! ✨
