-- Run this SQL manually in MySQL to fix Flyway
DELETE FROM flyway_schema_history WHERE version = '5' AND success = 0;
