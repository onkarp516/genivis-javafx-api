SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_dn_bill_details_tbl;
CREATE TABLE tranx_dn_bill_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_id BIGINT NULL,
   tranx_debitnote_master_id BIGINT NULL,
   tranx_debit_note_details BIGINT NULL,
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
   CONSTRAINT pk_tranx_dn_bill_details_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_dn_bill_details_tbl ADD CONSTRAINT FK_TRANX_DN_BILL_DETAILS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_dn_bill_details_tbl ADD CONSTRAINT FK_TRANX_DN_BILL_DETAILS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_dn_bill_details_tbl ADD CONSTRAINT FK_TRANX_DN_BILL_DETAILS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS tranx_cn_bill_details_tbl;
CREATE TABLE tranx_cn_bill_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_id BIGINT NULL,
   tranx_creditnote_master_id BIGINT NULL,
   tranx_credit_note_details BIGINT NULL,
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
   CONSTRAINT pk_tranx_cn_bill_details_tbl PRIMARY KEY (id)
);
SET FOREIGN_KEY_CHECKS = 1;








