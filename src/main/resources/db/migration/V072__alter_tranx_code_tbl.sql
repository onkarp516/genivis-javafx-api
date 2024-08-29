ALTER TABLE tranx_purchase_invoice_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_purchase_order_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_purchase_challan_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_pur_return_invoice_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_sales_quotation_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_sales_order_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_sales_challan_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_sales_invoice_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_sales_comp_invoice_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_counter_sales_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_sales_return_invoice_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_debit_note_new_reference_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_credit_note_new_reference_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_payment_master_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_receipt_master_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_journal_master_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_contra_master_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_gst_input_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;
ALTER TABLE tranx_gst_output_tbl
ADD COLUMN tranx_code VARCHAR(255) NULL;


