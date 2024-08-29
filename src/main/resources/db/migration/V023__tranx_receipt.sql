SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_receipt_master_tbl;
CREATE TABLE tranx_receipt_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   sales_invoice_id BIGINT NULL,
   receipt_no VARCHAR(255) NULL,
   receipt_sr_no DOUBLE NOT NULL,
   transcation_date datetime NULL,
   total_amt DOUBLE NOT NULL,
   status BIT(1) NOT NULL,
   narrations VARCHAR(255) NULL,
   financial_year VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   return_amt DOUBLE NULL,
   tranx_sales_invoice_id BIGINT NULL,
   CONSTRAINT pk_tranx_receipt_master_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_receipt_master_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_MASTER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_receipt_master_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_MASTER_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE tranx_receipt_master_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_MASTER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_receipt_master_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_MASTER_TBL_ON_SALES_INVOICE FOREIGN KEY (sales_invoice_id) REFERENCES tranx_sales_order_tbl (id);

ALTER TABLE tranx_receipt_master_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_MASTER_TBL_ON_TRANX_SALES_INVOICE FOREIGN KEY (tranx_sales_invoice_id) REFERENCES tranx_sales_invoice_tbl (id);

DROP TABLE if EXISTS tranx_receipt_perticulars_tbl;
CREATE TABLE tranx_receipt_perticulars_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_id BIGINT NULL,
   tranx_receipt_master_id BIGINT NULL,
   type VARCHAR(255) NULL,
   ledger_type VARCHAR(255) NULL,
   ledger_name VARCHAR(255) NULL,
   dr DOUBLE NULL,
   cr DOUBLE NULL,
   payment_method VARCHAR(255) NULL,
   payment_tranx_no VARCHAR(255) NULL,
   transaction_date date NULL,
   status BIT(1) NULL,
   payment_amount DOUBLE NULL,
   tranx_invoice_id BIGINT NULL,
   tranxtype VARCHAR(255) NULL,
   tranx_no VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   payable_amt DOUBLE NULL,
   selected_amt DOUBLE NULL,
   remaining_amt DOUBLE NULL,
   is_advance BIT(1) NULL,
   bank_name VARCHAR(255) NULL,
   payment_date date NULL,
   CONSTRAINT pk_tranx_receipt_perticulars_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_receipt_perticulars_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_PERTICULARS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_receipt_perticulars_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_PERTICULARS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_receipt_perticulars_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_PERTICULARS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_receipt_perticulars_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_PERTICULARS_TBL_ON_TRANX_RECEIPT_MASTER FOREIGN KEY (tranx_receipt_master_id) REFERENCES tranx_receipt_master_tbl (id);
DROP TABLE if EXISTS tranx_receipt_perticulars_details_tbl;
CREATE TABLE tranx_receipt_perticulars_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_id BIGINT NULL,
   tranx_receipt_master_id BIGINT NULL,
   tranx_receipt_perticulars_id BIGINT NULL,
   tranx_invoice_id BIGINT NULL,
   type VARCHAR(255) NULL,
   paid_amt DOUBLE NULL,
   transaction_date date NULL,
   tranx_no VARCHAR(255) NULL,
   status BIT(1) NULL,
   total_amt DOUBLE NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   remaining_amt DOUBLE NULL,
   amount DOUBLE NULL,
   balancing_type VARCHAR(255) NULL,
   CONSTRAINT pk_tranx_receipt_perticulars_details_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_receipt_perticulars_details_tbl ADD CONSTRAINT FK_TRANXRECEIPTPERTICULARSDETAILSTBL_ON_TRANXRECEIPTMASTER FOREIGN KEY (tranx_receipt_master_id) REFERENCES tranx_receipt_master_tbl (id);

ALTER TABLE tranx_receipt_perticulars_details_tbl ADD CONSTRAINT FK_TRANXRECEIPTPERTICULARSDETAILSTBL_ON_TRANXRECEIPTPERTICULARS FOREIGN KEY (tranx_receipt_perticulars_id) REFERENCES tranx_receipt_perticulars_tbl (id);

ALTER TABLE tranx_receipt_perticulars_details_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_PERTICULARS_DETAILS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_receipt_perticulars_details_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_PERTICULARS_DETAILS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_receipt_perticulars_details_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_PERTICULARS_DETAILS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;