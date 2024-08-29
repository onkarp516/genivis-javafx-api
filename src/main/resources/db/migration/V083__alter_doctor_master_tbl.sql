ALTER TABLE doctor_master_tbl
ADD qualification VARCHAR(255) NULL,
ADD register_no VARCHAR(255) NULL,
ADD commision BIGINT NULL,
ADD branch_id BIGINT NULL,
ADD outlet_id BIGINT NULL,
ADD created_at datetime NULL,
ADD created_by BIGINT NULL,
ADD updated_at datetime NULL,
ADD updated_by BIGINT NULL;