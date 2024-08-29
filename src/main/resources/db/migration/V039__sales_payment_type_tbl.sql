SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS sales_payment_type_tbl;
CREATE TABLE sales_payment_type_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   tranx_sales_invoice_id BIGINT NULL,
   type VARCHAR(255) NULL,
   label VARCHAR(255) NULL,
   created_by BIGINT NULL,
   payment_mode VARCHAR(255) NULL,
   payment_amount DOUBLE NULL,
   created_at datetime NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_sales_payment_type_tbl PRIMARY KEY (id)
);

ALTER TABLE sales_payment_type_tbl ADD CONSTRAINT FK_SALES_PAYMENT_TYPE_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE sales_payment_type_tbl ADD CONSTRAINT FK_SALES_PAYMENT_TYPE_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE sales_payment_type_tbl ADD CONSTRAINT FK_SALES_PAYMENT_TYPE_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE sales_payment_type_tbl ADD CONSTRAINT FK_SALES_PAYMENT_TYPE_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE sales_payment_type_tbl ADD CONSTRAINT FK_SALES_PAYMENT_TYPE_TBL_ON_TRANX_SALES_INVOICE FOREIGN KEY (tranx_sales_invoice_id) REFERENCES tranx_sales_invoice_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;