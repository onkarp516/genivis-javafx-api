ALTER TABLE tranx_receipt_master_tbl
ADD COLUMN gv_payment_tranx_no VARCHAR(255) NULL,
ADD COLUMN gv_bank_name VARCHAR(255) NULL,
ADD COLUMN gv_payment_mode VARCHAR(255) NULL,
ADD COLUMN gv_payment_date VARCHAR(255) NULL,
ADD COLUMN gv_bank_ledger_id BIGINT NULL,
ADD COLUMN is_fr_receipt BIT(1) NULL;





