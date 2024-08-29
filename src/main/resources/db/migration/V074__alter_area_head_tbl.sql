ALTER TABLE area_head_tbl
ADD COLUMN created_at datetime NULL,
ADD COLUMN created_by BIGINT NULL,
ADD COLUMN updated_at datetime NULL,
ADD COLUMN updated_by BIGINT NULL,
ADD COLUMN zone_state_head VARCHAR(255) NULL,
ADD COLUMN region_zone_head_id VARCHAR(255) NULL,
ADD COLUMN region_state_head_id VARCHAR(255) NULL,
ADD COLUMN district_region_head_id VARCHAR(255) NULL,
ADD COLUMN district_zone_head_id VARCHAR(255) NULL,
ADD COLUMN district_state_head_id VARCHAR(255) NULL;

