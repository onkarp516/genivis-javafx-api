ALTER TABLE sales_payment_type_tbl
ADD COLUMN payment_master_id BIGINT NULL,
ADD COLUMN reference_id VARCHAR(255) NULL,
ADD COLUMN customer_bank VARCHAR(255) NULL;
