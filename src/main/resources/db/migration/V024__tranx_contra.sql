SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_contra_master_tbl;
CREATE TABLE tranx_contra_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   contra_no VARCHAR(255) NULL,
   contra_sr_no DOUBLE NULL,
   transaction_date datetime NULL,
   total_amt DOUBLE NULL,
   status BIT(1) NULL,
   narrations VARCHAR(255) NULL,
   financial_year VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   CONSTRAINT pk_tranx_contra_master_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_contra_master_tbl ADD CONSTRAINT FK_TRANX_CONTRA_MASTER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_contra_master_tbl ADD CONSTRAINT FK_TRANX_CONTRA_MASTER_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE tranx_contra_master_tbl ADD CONSTRAINT FK_TRANX_CONTRA_MASTER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS tranx_contra_details_tbl;
CREATE TABLE tranx_contra_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_id BIGINT NULL,
   tranx_contra_master_id BIGINT NULL,
   type VARCHAR(255) NULL,
   ledger_type VARCHAR(255) NULL,
   ledger_name VARCHAR(255) NULL,
   paid_amount DOUBLE NULL,
   payment_type VARCHAR(255) NULL,
   bank_name VARCHAR(255) NULL,
   bank_payment_no VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   payment_date date NULL,
   CONSTRAINT pk_tranx_contra_details_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_contra_details_tbl ADD CONSTRAINT FK_TRANX_CONTRA_DETAILS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_contra_details_tbl ADD CONSTRAINT FK_TRANX_CONTRA_DETAILS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_contra_details_tbl ADD CONSTRAINT FK_TRANX_CONTRA_DETAILS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_contra_details_tbl ADD CONSTRAINT FK_TRANX_CONTRA_DETAILS_TBL_ON_TRANX_CONTRA_MASTER FOREIGN KEY (tranx_contra_master_id) REFERENCES tranx_contra_master_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;