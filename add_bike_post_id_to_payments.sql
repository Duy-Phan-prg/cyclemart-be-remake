-- Add bike_post_id column to payments table
ALTER TABLE payments ADD COLUMN bike_post_id BIGINT;

-- Add foreign key constraint
ALTER TABLE payments ADD CONSTRAINT fk_payments_bike_post 
FOREIGN KEY (bike_post_id) REFERENCES bike_posts(id);

-- Create index for better query performance
CREATE INDEX idx_payments_bike_post_id ON payments(bike_post_id);
