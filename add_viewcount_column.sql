-- Add viewCount column to bike_posts table
ALTER TABLE bike_posts
ADD COLUMN view_count INT DEFAULT 0 NOT NULL;
