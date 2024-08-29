SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_sales_return_adj_bills_tbl;
CREATE TABLE tranx_sales_return_adj_bills_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   tranx_sales_invoice_id BIGINT NULL,
   tranx_sales_challan_id BIGINT NULL,
   paid_amt DOUBLE NULL,
   remaining_amt DOUBLE NULL,
   total_amt DOUBLE NULL,
   source VARCHAR(255) NULL,
   status BIT(1) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   tranx_sales_return_id BIGINT NULL,
   CONSTRAINT pk_tranx_sales_return_adj_bills_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_sales_return_adj_bills_tbl ADD CONSTRAINT FK_TRANX_SALES_RETURN_ADJ_BILLS_TBL_ON_TRANX_SALES_CHALLAN FOREIGN KEY (tranx_sales_challan_id) REFERENCES tranx_sales_challan_tbl (id);

ALTER TABLE tranx_sales_return_adj_bills_tbl ADD CONSTRAINT FK_TRANX_SALES_RETURN_ADJ_BILLS_TBL_ON_TRANX_SALES_INVOICE FOREIGN KEY (tranx_sales_invoice_id) REFERENCES tranx_sales_invoice_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;