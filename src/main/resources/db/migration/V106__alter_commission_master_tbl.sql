ALTER TABLE commission_master_tbl
ADD COLUMN tds_per DOUBLE NULL;

ALTER TABLE franchise_master_tbl
ADD COLUMN is_funded BIT(1) NULL,
ADD COLUMN fund_amt DOUBLE NULL;
