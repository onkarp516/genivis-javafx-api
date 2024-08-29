SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_sales_invoice_tbl;
CREATE TABLE tranx_sales_invoice_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   sundry_debtors_id BIGINT NULL,
   sales_account_ledger_id BIGINT NULL,
   sales_discount_ledger_id BIGINT NULL,
   sales_roundoff_id BIGINT NULL,
   associates_groups_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   sales_serial_number BIGINT NULL,
   sales_invoice_no VARCHAR(255) NULL,
   bill_date datetime NULL,
   transport_name VARCHAR(255) NULL,
   reference VARCHAR(255) NULL,
   round_off DOUBLE NULL,
   total_base_amount DOUBLE NULL,
   total_amount DOUBLE NULL,
   totalcgst DOUBLE NULL,
   totalqty BIGINT NULL,
   totalsgst DOUBLE NULL,
   totaligst DOUBLE NULL,
   sales_discount_amount DOUBLE NULL,
   sales_discount_per DOUBLE NULL,
   total_sales_discount_amt DOUBLE NULL,
   additional_charges_total DOUBLE NULL,
   taxable_amount DOUBLE NULL,
   tcs DOUBLE NULL,
   is_counter_sale BIT(1) NULL,
   counter_sale_id VARCHAR(255) NULL,
   status BIT(1) NULL,
   financial_year VARCHAR(255) NULL,
   narration VARCHAR(255) NULL,
   operations VARCHAR(255) NULL,
   reference_sq_id VARCHAR(255) NULL,
   reference_so_id VARCHAR(255) NULL,
   reference_sc_id VARCHAR(255) NULL,
   created_by BIGINT NULL,
   payment_mode VARCHAR(255) NULL,
   payment_amount DOUBLE NULL,
   cash DOUBLE NULL,
   digital DOUBLE NULL,
   card_payment DOUBLE NULL,
   advanced_amount DOUBLE NULL,
   gst_number VARCHAR(255) NULL,
   created_at datetime NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   balance DOUBLE NULL,
   additional_ledger_id1 BIGINT NULL,
   additional_ledger_id2 BIGINT NULL,
   additional_ledger_id3 BIGINT NULL,
   addition_ledger_amt1 DOUBLE NULL,
   addition_ledger_amt2 DOUBLE NULL,
   addition_ledger_amt3 DOUBLE NULL,
   free_qty DOUBLE NULL,
   gross_amount DOUBLE NULL,
   total_tax DOUBLE NULL,
   saleman_id BIGINT NULL,
   barcode VARCHAR(255) NULL,
   salesman_user BIGINT NULL,
   transaction_status BIGINT NULL,
   image_path VARCHAR(255) NULL,
   is_selected BIT(1) NULL,
   is_credit_note_ref BIT(1) NULL,
   is_round_off BIT(1) NULL,
   tcs_amt DOUBLE NULL,
   tcs_mode VARCHAR(255) NULL,
   tds_amt DOUBLE NULL,
   tds_per DOUBLE NULL,
   CONSTRAINT pk_tranx_sales_invoice_tbl PRIMARY KEY (id)
);
ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_ADDITIONAL_LEDGER_ID1 FOREIGN KEY (additional_ledger_id1) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_ADDITIONAL_LEDGER_ID2 FOREIGN KEY (additional_ledger_id2) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_ADDITIONAL_LEDGER_ID3 FOREIGN KEY (additional_ledger_id3) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_ASSOCIATES_GROUPS FOREIGN KEY (associates_groups_id) REFERENCES associates_groups_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_SALEMAN FOREIGN KEY (saleman_id) REFERENCES users_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_SALES_ACCOUNT_LEDGER FOREIGN KEY (sales_account_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_SALES_DISCOUNT_LEDGER FOREIGN KEY (sales_discount_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_SALES_ROUNDOFF FOREIGN KEY (sales_roundoff_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_invoice_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_TBL_ON_SUNDRY_DEBTORS FOREIGN KEY (sundry_debtors_id) REFERENCES ledger_master_tbl (id);

DROP TABLE if EXISTS tranx_sales_duties_taxes_tbl;
CREATE TABLE tranx_sales_duties_taxes_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   sales_invoice_id BIGINT NOT NULL,
   sundry_debtors_id BIGINT NOT NULL,
   duties_taxes_ledger_id BIGINT NULL,
   intra BIT(1) NULL,
   amount DOUBLE NULL,
   status BIT(1) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   CONSTRAINT pk_tranx_sales_duties_taxes_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_sales_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_SALES_DUTIES_TAXES_TBL_ON_DUTIES_TAXES_LEDGER FOREIGN KEY (duties_taxes_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_SALES_DUTIES_TAXES_TBL_ON_SALES_INVOICE FOREIGN KEY (sales_invoice_id) REFERENCES tranx_sales_invoice_tbl (id);

ALTER TABLE tranx_sales_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_SALES_DUTIES_TAXES_TBL_ON_SUNDRY_DEBTORS FOREIGN KEY (sundry_debtors_id) REFERENCES ledger_master_tbl (id);

DROP TABLE if EXISTS tranx_sales_invoice_additional_charges_tbl;
CREATE TABLE tranx_sales_invoice_additional_charges_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   sales_invoice_id BIGINT NULL,
   additional_charges_id BIGINT NULL,
   amount DOUBLE NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   operation VARCHAR(255) NULL,
   CONSTRAINT pk_tranx_sales_invoice_additional_charges_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_sales_invoice_additional_charges_tbl ADD CONSTRAINT FK_TRANXSALESINVOICEADDITIONALCHARGESTBL_ON_ADDITIONALCHARGES FOREIGN KEY (additional_charges_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_invoice_additional_charges_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_ADDITIONAL_CHARGES_TBL_ON_SALES_INVOICE FOREIGN KEY (sales_invoice_id) REFERENCES tranx_sales_invoice_tbl (id);


DROP TABLE if EXISTS tranx_sales_invoice_details_units_tbl;
CREATE TABLE tranx_sales_invoice_details_units_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   sales_invoice_id BIGINT NULL,
   product_id BIGINT NULL,
   unit_id BIGINT NULL,
   batch_id BIGINT NULL,
   transaction_status_id BIGINT NULL,
   unit_conversions DOUBLE NULL,
   qty DOUBLE NULL,
   rate DOUBLE NULL,
   base_amt DOUBLE NULL,
   total_amount DOUBLE NULL,
   discount_amount DOUBLE NULL,
   discount_per DOUBLE NULL,
   discount_amount_cal DOUBLE NULL,
   discount_per_cal DOUBLE NULL,
   igst DOUBLE NULL,
   sgst DOUBLE NULL,
   cgst DOUBLE NULL,
   total_igst DOUBLE NULL,
   total_sgst DOUBLE NULL,
   total_cgst DOUBLE NULL,
   final_amount DOUBLE NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   status BIT(1) NULL,
   free_qty DOUBLE NULL,
   discountbin_per DOUBLE NULL,
   total_discount_in_amt DOUBLE NULL,
   gross_amt DOUBLE NULL,
   addition_charges_amt DOUBLE NULL,
   gross_amt1 DOUBLE NULL,
   invoice_dis_amt DOUBLE NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   return_qty DOUBLE NULL,
   CONSTRAINT pk_tranx_sales_invoice_details_units_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_sales_invoice_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_DETAILS_UNITS_TBL_ON_BATCH FOREIGN KEY (batch_id) REFERENCES product_batchno_tbl (id);

ALTER TABLE tranx_sales_invoice_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_DETAILS_UNITS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_sales_invoice_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_DETAILS_UNITS_TBL_ON_SALES_INVOICE FOREIGN KEY (sales_invoice_id) REFERENCES tranx_sales_invoice_tbl (id);

ALTER TABLE tranx_sales_invoice_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_DETAILS_UNITS_TBL_ON_UNIT FOREIGN KEY (unit_id) REFERENCES units_tbl (id);

DROP TABLE if EXISTS tranx_sales_invoice_product_sr_no_tbl;
CREATE TABLE tranx_sales_invoice_product_sr_no_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NOT NULL,
   product_id BIGINT NOT NULL,
   transaction_type_master_id BIGINT NULL,
   serial_no VARCHAR(255) NULL,
   sale_created_at datetime NULL,
   transaction_status VARCHAR(255) NULL,
   operations VARCHAR(255) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   status BIT(1) NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   sales_inv_unit_details_id BIGINT NULL,
   units_id BIGINT NULL,
   CONSTRAINT pk_tranx_sales_invoice_product_sr_no_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_sales_invoice_product_sr_no_tbl ADD CONSTRAINT FK_TRANXSALESINVOICEPRODUCTSRNOTBL_ON_SALESINVUNITDETAILS FOREIGN KEY (sales_inv_unit_details_id) REFERENCES tranx_sales_invoice_details_units_tbl (id);

ALTER TABLE tranx_sales_invoice_product_sr_no_tbl ADD CONSTRAINT FK_TRANXSALESINVOICEPRODUCTSRNOTBL_ON_TRANSACTIONTYPEMASTER FOREIGN KEY (transaction_type_master_id) REFERENCES transaction_type_master_tbl (id);

ALTER TABLE tranx_sales_invoice_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_PRODUCT_SR_NO_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_sales_invoice_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_PRODUCT_SR_NO_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE tranx_sales_invoice_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_PRODUCT_SR_NO_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE tranx_sales_invoice_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_PRODUCT_SR_NO_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

ALTER TABLE tranx_sales_invoice_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_PRODUCT_SR_NO_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_sales_invoice_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_PRODUCT_SR_NO_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_sales_invoice_product_sr_no_tbl ADD CONSTRAINT FK_TRANX_SALES_INVOICE_PRODUCT_SR_NO_TBL_ON_UNITS FOREIGN KEY (units_id) REFERENCES units_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;