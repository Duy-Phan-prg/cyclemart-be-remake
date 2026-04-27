-- bike_post_id trong seller_ratings chỉ là context tham khảo, không bắt buộc
-- Rating là đánh giá dịch vụ của seller, không phải đánh giá sản phẩm
ALTER TABLE seller_ratings MODIFY COLUMN bike_post_id BIGINT NULL;
