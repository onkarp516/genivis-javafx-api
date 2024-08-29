ALTER TABLE tranx_purchase_order_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;

ALTER TABLE tranx_purchase_challan_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;

ALTER TABLE tranx_purchase_invoice_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;

ALTER TABLE tranx_pur_return_invoice_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;


ALTER TABLE tranx_counter_sales_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;

ALTER TABLE tranx_sales_order_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;

ALTER TABLE tranx_sales_quotation_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;

ALTER TABLE tranx_sales_challan_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;

ALTER TABLE tranx_sales_invoice_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;

ALTER TABLE tranx_sales_comp_invoice_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;

ALTER TABLE tranx_sales_return_invoice_tbl
ADD transaction_tracking_no VARCHAR(255) NULL;