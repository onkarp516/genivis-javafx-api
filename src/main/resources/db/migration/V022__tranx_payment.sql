SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_payment_master_tbl;
CREATE TABLE tranx_payment_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   payment_no VARCHAR(255) NULL,
   payment_sr_no DOUBLE NULL,
   transcation_date datetime NULL,
   total_amt DOUBLE NULL,
   status BIT(1) NULL,
   narrations VARCHAR(255) NULL,
   financial_year VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   CONSTRAINT pk_tranx_payment_master_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_payment_master_tbl ADD CONSTRAINT FK_TRANX_PAYMENT_MASTER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_payment_master_tbl ADD CONSTRAINT FK_TRANX_PAYMENT_MASTER_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE tranx_payment_master_tbl ADD CONSTRAINT FK_TRANX_PAYMENT_MASTER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS tranx_payment_perticulars_tbl;
CREATE TABLE tranx_payment_perticulars_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_id BIGINT NULL,
   tranx_payment_master_id BIGINT NULL,
   type VARCHAR(255) NULL,
   ledger_type VARCHAR(255) NULL,
   ledger_name VARCHAR(255) NULL,
   dr DOUBLE NULL,
   cr DOUBLE NULL,
   payment_method VARCHAR(255) NULL,
   payment_tranx_no VARCHAR(255) NULL,
   transaction_date datetime NULL,
   status BIT(1) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   payable_amt DOUBLE NULL,
   selected_amt DOUBLE NULL,
   remaining_amt DOUBLE NULL,
   is_advance BIT(1) NULL,
   bank_name VARCHAR(255) NULL,
   payment_date date NULL,
   CONSTRAINT pk_tranx_payment_perticulars_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_payment_perticulars_tbl ADD CONSTRAINT FK_TRANX_PAYMENT_PERTICULARS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_payment_perticulars_tbl ADD CONSTRAINT FK_TRANX_PAYMENT_PERTICULARS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_payment_perticulars_tbl ADD CONSTRAINT FK_TRANX_PAYMENT_PERTICULARS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_payment_perticulars_tbl ADD CONSTRAINT FK_TRANX_PAYMENT_PERTICULARS_TBL_ON_TRANX_PAYMENT_MASTER FOREIGN KEY (tranx_payment_master_id) REFERENCES tranx_payment_master_tbl (id);

DROP TABLE if EXISTS tranx_payment_perticulars_details_tbl;
CREATE TABLE tranx_payment_perticulars_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_id BIGINT NULL,
   tranx_payment_master_id BIGINT NULL,
   tranx_payment_perticulars_id BIGINT NULL,
   tranx_invoice_id BIGINT NULL,
   type VARCHAR(255) NULL,
   paid_amt DOUBLE NULL,
   transaction_date datetime NULL,
   tranx_no VARCHAR(255) NULL,
   status BIT(1) NULL,
   total_amt DOUBLE NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   remaining_amt DOUBLE NULL,
   amount DOUBLE NULL,
   balancing_type VARCHAR(255) NULL,
   CONSTRAINT pk_tranx_payment_perticulars_details_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_payment_perticulars_details_tbl ADD CONSTRAINT FK_TRANXPAYMENTPERTICULARSDETAILSTBL_ON_TRANXPAYMENTMASTER FOREIGN KEY (tranx_payment_master_id) REFERENCES tranx_payment_master_tbl (id);

ALTER TABLE tranx_payment_perticulars_details_tbl ADD CONSTRAINT FK_TRANXPAYMENTPERTICULARSDETAILSTBL_ON_TRANXPAYMENTPERTICULARS FOREIGN KEY (tranx_payment_perticulars_id) REFERENCES tranx_payment_perticulars_tbl (id);

ALTER TABLE tranx_payment_perticulars_details_tbl ADD CONSTRAINT FK_TRANX_PAYMENT_PERTICULARS_DETAILS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_payment_perticulars_details_tbl ADD CONSTRAINT FK_TRANX_PAYMENT_PERTICULARS_DETAILS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_payment_perticulars_details_tbl ADD CONSTRAINT FK_TRANX_PAYMENT_PERTICULARS_DETAILS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;