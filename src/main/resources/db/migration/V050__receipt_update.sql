ALTER TABLE tranx_receipt_master_tbl
ADD tranx_sales_comp_invoice_id BIGINT NULL,
ADD invoice_type VARCHAR(255) NULL;
ALTER TABLE tranx_receipt_master_tbl ADD CONSTRAINT FK_TRANX_RECEIPT_MASTER_TBL_ON_TRANX_SALES_COMP_INVOICE FOREIGN KEY (tranx_sales_comp_invoice_id) REFERENCES tranx_sales_comp_invoice_tbl (id);

ALTER TABLE sales_payment_type_tbl
ADD tranx_sales_comp_invoice_id BIGINT NULL,
ADD invoice_type VARCHAR(255) NULL;
ALTER TABLE sales_payment_type_tbl ADD CONSTRAINT FK_SALES_PAYMENT_TYPE_TBL_ON_TRANX_SALES_COMP_INVOICE FOREIGN KEY (tranx_sales_comp_invoice_id) REFERENCES tranx_sales_comp_invoice_tbl (id);
