ALTER TABLE tranx_sales_return_addi_charges_tbl
ADD COLUMN percent DOUBLE null;

ALTER TABLE tranx_pur_return_invoice_additional_charges_tbl
ADD percent DOUBLE NULL;

ALTER TABLE tranx_purchase_challan_additional_charges_tbl
ADD COLUMN percent DOUBLE null;

ALTER TABLE tranx_sales_challan_additional_charges_tbl
ADD COLUMN percent DOUBLE null;
