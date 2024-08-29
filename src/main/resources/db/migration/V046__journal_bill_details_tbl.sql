SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_journal_bill_details_tbl;
CREATE TABLE tranx_journal_bill_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id INT NULL,
   outlet_id INT NULL,
   ledger_id BIGINT NULL,
   tranx_journal_master_id BIGINT NULL,
   tranx_journal_details_id BIGINT NULL,
   tranx_invoice_id BIGINT NULL,
   type VARCHAR(255) NULL,
   paid_amt DOUBLE NULL,
   transaction_date date NULL,
   tranx_no VARCHAR(255) NULL,
   status BIT(1) NULL,
   total_amt DOUBLE NULL,
   remaining_amt DOUBLE NULL,
   amount DOUBLE NULL,
   balancing_type VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   CONSTRAINT pk_tranx_journal_bill_details_tbl PRIMARY KEY (id)
);
ALTER TABLE tranx_journal_details_tbl
  ADD payable_amt DOUBLE NULL,
  ADD selected_amt DOUBLE NULL,
  ADD remaining_amt DOUBLE NULL,
  ADD is_advance BIT(1) NULL,
  ADD bank_name VARCHAR(255) NULL,
  ADD payment_date date NULL,
  ADD cr DOUBLE NULL,
  ADD dr DOUBLE NULL,
  ADD payment_method VARCHAR(255) NULL,
  ADD payment_tranx_no VARCHAR(255) NULL,
  ADD transaction_date date NULL;
SET FOREIGN_KEY_CHECKS = 1;








