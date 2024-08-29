SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS ledger_opening_balance_tbl;
CREATE TABLE ledger_opening_balance_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   invoice_no VARCHAR(255) NULL,
   invoice_date date NULL,
   due_days BIGINT NULL,
   bill_amt DOUBLE NULL,
   invoice_paid_amt DOUBLE NULL,
   invoice_bal_amt DOUBLE NULL,
   invoice_bal_type VARCHAR(255) NULL,
   ledger_id BIGINT NULL,
   balancing_type VARCHAR(255) NULL,
   status BIT(1) NULL,
   created_by BIGINT NULL,
   updated_by BIGINT NULL,
   created_at date NULL,
   updated_at date NULL,
   CONSTRAINT pk_ledger_opening_balance_tbl PRIMARY KEY (id)
);
SET FOREIGN_KEY_CHECKS = 1;





