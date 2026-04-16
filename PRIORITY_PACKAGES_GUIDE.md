# Hướng Dẫn Sử Dụng Chức Năng Gói Ưu Tiên Hiển Thị

## Tổng Quan

Chức năng Gói Ưu Tiên cho phép chủ sở hữu bài post có thể nâng cao độ ưu tiên hiển thị của bài post trên nền tảng thông qua các gói ưu tiên được định sẵn.

## Cấu Trúc Entities

### 1. PriorityPackage
Đại diện cho các gói ưu tiên có sẵn trên hệ thống:
- `id`: ID gói ưu tiên
- `name`: Tên gói (vd: "Gói VIP", "Gói Premium")
- `description`: Mô tả chi tiết
- `price`: Giá của gói (VND)
- `durationDays`: Thời hạn hiệu lực (ngày)
- `priorityLevel`: Mức ưu tiên (1-10, 10 là cao nhất)
- `displayColor`: Màu hiển thị trên UI (Hex: #RRGGBB)
- `isActive`: Trạng thái hoạt động

### 2. PostPrioritySubscription
Đại diện cho việc bài post được đăng ký với một gói ưu tiên:
- `id`: ID đăng ký
- `post`: Liên kết tới BikePost
- `priorityPackage`: Liên kết tới PriorityPackage
- `startDate`: Ngày bắt đầu
- `endDate`: Ngày kết thúc (tính dựa trên `durationDays`)
- `isActive`: Trạng thái hoạt động

### 3. BikePost
Đã được cập nhật với:
- `prioritySubscriptions`: List các đăng ký ưu tiên của bài post

## API Endpoints

### Priority Package Management

#### 1. Tạo Gói Ưu Tiên
```
POST /api/v1/priority-packages
Content-Type: application/json

{
  "name": "Gói VIP",
  "description": "Hiển thị nổi bật trên trang chủ",
  "price": 50000,
  "durationDays": 7,
  "priorityLevel": 5,
  "displayColor": "#FFD700",
  "isActive": true
}
```

#### 2. Cập Nhật Gói Ưu Tiên
```
PUT /api/v1/priority-packages/{id}
Content-Type: application/json

{
  "name": "Gói VIP Pro",
  "description": "Hiển thị nổi bật với ưu đãi đặc biệt",
  "price": 75000,
  "durationDays": 14,
  "priorityLevel": 8,
  "displayColor": "#FFD700",
  "isActive": true
}
```

#### 3. Xóa Gói Ưu Tiên
```
DELETE /api/v1/priority-packages/{id}
```

#### 4. Lấy Thông Tin Gói
```
GET /api/v1/priority-packages/{id}
```

#### 5. Lấy Danh Sách Tất Cả Gói
```
GET /api/v1/priority-packages
```

#### 6. Lấy Danh Sách Gói Hoạt Động
```
GET /api/v1/priority-packages/active
```

### Post Priority Subscription Management

#### 1. Đăng Ký Bài Post Với Gói Ưu Tiên
```
POST /api/v1/post-priority-subscriptions
Content-Type: application/json

{
  "postId": 1,
  "packageId": 1
}
```

**Response:**
```json
{
  "id": 1,
  "postId": 1,
  "postTitle": "Xe đạp Giant Defy Advanced 2",
  "packageName": "Gói VIP",
  "priorityLevel": 5,
  "displayColor": "#FFD700",
  "startDate": "2024-04-16T10:30:00",
  "endDate": "2024-04-23T10:30:00",
  "isActive": true,
  "createdAt": "2024-04-16T10:30:00",
  "updatedAt": "2024-04-16T10:30:00"
}
```

#### 2. Hủy Đăng Ký Ưu Tiên
```
DELETE /api/v1/post-priority-subscriptions/{id}
```

#### 3. Lấy Thông Tin Đăng Ký
```
GET /api/v1/post-priority-subscriptions/{id}
```

#### 4. Lấy Tất Cả Đăng Ký Hoạt Động Của Bài Post
```
GET /api/v1/post-priority-subscriptions/post/{postId}
```

#### 5. Lấy Tất Cả Đăng Ký Hoạt Động (Sắp Xếp Theo Ưu Tiên)
```
GET /api/v1/post-priority-subscriptions/active
```

Trả về danh sách các đăng ký sắp xếp theo `priorityLevel` giảm dần.

#### 6. Kiểm Tra Bài Post Có Ưu Tiên Hoạt Động
```
GET /api/v1/post-priority-subscriptions/post/{postId}/has-priority
```

**Response:** `true` hoặc `false`

#### 7. Hết Hạn Tất Cả Đăng Ký Đã Qua Thời Gian
```
POST /api/v1/post-priority-subscriptions/expire-expired
```

*Chú ý: Endpoint này nên được gọi định kỳ (ví dụ: hàng ngày) để tự động hết hạn các đăng ký.*

## BikePost Response Update

Khi lấy thông tin bài post, response sẽ bao gồm thông tin ưu tiên hoạt động cao nhất:

```json
{
  "id": 1,
  "title": "Xe đạp Giant Defy Advanced 2",
  "description": "...",
  "price": 15000000,
  "status": "LIKE_NEW",
  "city": "HO_CHI_MINH",
  "district": "QUAN_1",
  "brand": "GIANT",
  "model": "Defy Advanced 2",
  "year": 2024,
  "frameMaterial": "CARBON",
  "frameSize": "M",
  "brakeType": "DISC_HYDRAULIC",
  "groupset": "SHIMANO_105",
  "mileage": 2500,
  "categoryName": "Road Bike",
  "allowNegotiation": true,
  "images": ["url1", "url2"],
  "activePriority": {
    "id": 1,
    "name": "Gói VIP",
    "description": "Hiển thị nổi bật",
    "price": 50000,
    "durationDays": 7,
    "priorityLevel": 5,
    "displayColor": "#FFD700",
    "isActive": true,
    "createdAt": "2024-04-16T10:30:00",
    "updatedAt": "2024-04-16T10:30:00"
  },
  "createdAt": "2024-04-16T10:30:00"
}
```

Nếu bài post không có ưu tiên hoạt động, `activePriority` sẽ là `null`.

## Business Logic

### Quy Tắc Đăng Ký
1. **Chỉ có một gói ưu tiên hoạt động mỗi lần**: Nếu bài post đã có đăng ký với một gói, không thể đăng ký gói khác trùng lặp.
2. **Thời hạn tự động**: Ngày kết thúc = Ngày bắt đầu + `durationDays`
3. **Gói phải hoạt động**: Chỉ có thể đăng ký gói có `isActive = true`
4. **Bài post phải tồn tại**: Kiểm tra bài post có tồn tại trước khi đăng ký

### Ưu Tiên Hiển Thị
- Bài post với `priorityLevel` cao hơn sẽ được ưu tiên hiển thị
- Sử dụng `displayColor` để phân biệt gói ưu tiên trên UI

### Hết Hạn Đăng Ký
- Khi `endDate` < `CURRENT_TIMESTAMP`, đăng ký tự động không còn hoạt động
- Nên gọi endpoint `/expire-expired` định kỳ để cập nhật trạng thái

## Các Ví Dụ Sử Dụng

### Ví Dụ 1: Tạo Gói Ưu Tiên Mới
```bash
curl -X POST http://localhost:8080/api/v1/priority-packages \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gói Premium",
    "description": "Nổi bật trên trang chủ với badge đặc biệt",
    "price": 100000,
    "durationDays": 30,
    "priorityLevel": 9,
    "displayColor": "#FF6B6B",
    "isActive": true
  }'
```

### Ví Dụ 2: Đăng Ký Bài Post Với Gói
```bash
curl -X POST http://localhost:8080/api/v1/post-priority-subscriptions \
  -H "Content-Type: application/json" \
  -d '{
    "postId": 5,
    "packageId": 2
  }'
```

### Ví Dụ 3: Lấy Bài Post Với Thông Tin Ưu Tiên
```bash
curl -X GET http://localhost:8080/api/v1/posts/5
```

### Ví Dụ 4: Lấy Tất Cả Bài Post Có Ưu Tiên (Sắp Xếp)
```bash
curl -X GET http://localhost:8080/api/v1/post-priority-subscriptions/active
```

## Integration Với BikePost API

BikePost API đã được tích hợp để:
1. **Trả về thông tin ưu tiên**: Mỗi khi lấy thông tin bài post, sẽ kèm theo gói ưu tiên hoạt động cao nhất
2. **Lọc theo ưu tiên**: Frontend có thể sử dụng thông tin này để sắp xếp/hiển thị bài post
3. **Hiển thị UI**: Sử dụng `displayColor` và `priorityLevel` để render UI khác nhau

## Lưu Ý Kỹ Thuật

- **Cascading Delete**: Khi xóa PriorityPackage, tất cả PostPrioritySubscription liên quan sẽ tự động bị xóa
- **Lazy Loading**: Nên cẩn thận với N+1 queries khi lấy danh sách bài post
- **Validation**: Tất cả input đều được validate theo các rules đã định
- **Timestamp**: Sử dụng LocalDateTime cho tất cả timestamp (lưu theo UTC)

## Maintenance

### Scheduled Task (Recommended)
Nên tạo một scheduled task để hết hạn các đăng ký hàng ngày:

```java
@Scheduled(cron = "0 0 0 * * *") // Chạy lúc 00:00 mỗi ngày
public void expireExpiredSubscriptions() {
    postPrioritySubscriptionService.expireExpiredSubscriptions();
}
```
