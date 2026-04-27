-- Chuyển cột type từ ENUM sang VARCHAR để nhận giá trị mới (DIRECT_PAYMENT, v.v.)
ALTER TABLE payments MODIFY COLUMN type VARCHAR(50);
