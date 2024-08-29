ALTER TABLE inventory_details_postings_tbl
ADD COLUMN operation VARCHAR(255) NULL,
ADD COLUMN tranx_code VARCHAR(255) NULL;


ALTER TABLE ledger_transaction_postings_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;

