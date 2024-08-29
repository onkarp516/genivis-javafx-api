SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_sales_order_tbl;
CREATE TABLE tranx_sales_order_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   sundry_debtors_id BIGINT NULL,
   sales_account_ledger_id BIGINT NULL,
   sales_roundoff_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   transaction_status_id BIGINT NOT NULL,
   sales_order_sr_no BIGINT NULL,
   bill_date datetime NULL,
   reference VARCHAR(255) NULL,
   so_bill_no VARCHAR(255) NULL,
   sq_ref_id VARCHAR(255) NULL,
   round_off DOUBLE NULL,
   total_base_amount DOUBLE NULL,
   total_amount DOUBLE NULL,
   totalcgst DOUBLE NULL,
   totalqty BIGINT NULL,
   totalsgst DOUBLE NULL,
   totaligst DOUBLE NULL,
   additional_charges_total DOUBLE NULL,
   taxable_amount DOUBLE NULL,
   tcs DOUBLE NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   financial_year VARCHAR(255) NULL,
   operations VARCHAR(255) NULL,
   customer_name VARCHAR(255) NULL,
   mobile_no BIGINT NULL,
   payment_mode VARCHAR(255) NULL,
   advanced_amount DOUBLE NULL,
   created_date datetime NULL,
   updated_by BIGINT NULL,
   updated_date datetime NULL,
   narration VARCHAR(255) NULL,
   ledger_id BIGINT NULL,
   ledger_amt DOUBLE NULL,
   saleman_id BIGINT NULL,
   barcode VARCHAR(255) NULL,
   free_qty DOUBLE NULL,
   gst_number VARCHAR(255) NULL,
   additional_ledger_id1 BIGINT NULL,
   additional_ledger_id2 BIGINT NULL,
   additional_ledger_id3 BIGINT NULL,
   addition_ledger_amt1 DOUBLE NULL,
   addition_ledger_amt2 DOUBLE NULL,
   addition_ledger_amt3 DOUBLE NULL,
   gross_amount DOUBLE NULL,
   total_tax DOUBLE NULL,
   CONSTRAINT pk_tranx_sales_order_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_ADDITIONAL_LEDGER_ID1 FOREIGN KEY (additional_ledger_id1) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_ADDITIONAL_LEDGER_ID2 FOREIGN KEY (additional_ledger_id2) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_ADDITIONAL_LEDGER_ID3 FOREIGN KEY (additional_ledger_id3) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_SALEMAN FOREIGN KEY (saleman_id) REFERENCES users_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_SALES_ACCOUNT_LEDGER FOREIGN KEY (sales_account_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_SALES_ROUNDOFF FOREIGN KEY (sales_roundoff_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_SUNDRY_DEBTORS FOREIGN KEY (sundry_debtors_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_order_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_TBL_ON_TRANSACTION_STATUS FOREIGN KEY (transaction_status_id) REFERENCES transaction_status_tbl (id);


DROP TABLE if EXISTS tranx_sales_order_details_tbl;
CREATE TABLE tranx_sales_order_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   sales_order_invoice_id BIGINT NULL,
   product_id BIGINT NULL,
   packaging_id BIGINT NULL,
   base_amt DOUBLE NULL,
   total_amount DOUBLE NULL,
   discount_amount DOUBLE NULL,
   reference_id VARCHAR(255) NULL,
   reference_type VARCHAR(255) NULL,
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
   qty_high DOUBLE NULL,
   rate_high DOUBLE NULL,
   qty_medium DOUBLE NULL,
   rate_medium DOUBLE NULL,
   qty_low DOUBLE NULL,
   rate_low DOUBLE NULL,
   base_amt_high DOUBLE NULL,
   base_amt_low DOUBLE NULL,
   base_amt_medium DOUBLE NULL,
   status BIT(1) NULL,
   operations VARCHAR(255) NULL,
   created_date datetime NULL,
   created_by BIGINT NULL,
   updated_date datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_tranx_sales_order_details_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_sales_order_details_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_TBL_ON_PACKAGING FOREIGN KEY (packaging_id) REFERENCES packing_master_tbl (id);

ALTER TABLE tranx_sales_order_details_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_sales_order_details_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_TBL_ON_SALES_ORDER_INVOICE FOREIGN KEY (sales_order_invoice_id) REFERENCES tranx_sales_order_tbl (id);


DROP TABLE if EXISTS tranx_sales_order_duties_taxes_tbl;
CREATE TABLE tranx_sales_order_duties_taxes_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   sales_order_invoice_id BIGINT NOT NULL,
   sundry_debtors_id BIGINT NOT NULL,
   duties_taxes_ledger_id BIGINT NOT NULL,
   intra BIT(1) NULL,
   amount DOUBLE NULL,
   status BIT(1) NULL,
   created_date datetime NULL,
   CONSTRAINT pk_tranx_sales_order_duties_taxes_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_sales_order_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DUTIES_TAXES_TBL_ON_DUTIES_TAXES_LEDGER FOREIGN KEY (duties_taxes_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_sales_order_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DUTIES_TAXES_TBL_ON_SALES_ORDER_INVOICE FOREIGN KEY (sales_order_invoice_id) REFERENCES tranx_sales_order_tbl (id);

ALTER TABLE tranx_sales_order_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DUTIES_TAXES_TBL_ON_SUNDRY_DEBTORS FOREIGN KEY (sundry_debtors_id) REFERENCES ledger_master_tbl (id);

DROP TABLE if EXISTS tranx_sales_order_details_units_tbl;
CREATE TABLE tranx_sales_order_details_units_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   sales_order_id BIGINT NULL,
   sales_order_details_id BIGINT NULL,
   product_id BIGINT NULL,
   unit_id BIGINT NULL,
   packaging_id BIGINT NULL,
   flavour_master_id BIGINT NULL,
   brand_id BIGINT NULL,
   group_id BIGINT NULL,
   category_id BIGINT NULL,
   subcategory_id BIGINT NULL,
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
   transaction_status BIGINT NULL,
   CONSTRAINT pk_tranx_sales_order_details_units_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_FLAVOUR_MASTER FOREIGN KEY (flavour_master_id) REFERENCES flavour_master_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_GROUP FOREIGN KEY (group_id) REFERENCES group_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_PACKAGING FOREIGN KEY (packaging_id) REFERENCES packing_master_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_SALES_ORDER FOREIGN KEY (sales_order_id) REFERENCES tranx_sales_order_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_SALES_ORDER_DETAILS FOREIGN KEY (sales_order_details_id) REFERENCES tranx_sales_order_details_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES sub_category_tbl (id);

ALTER TABLE tranx_sales_order_details_units_tbl ADD CONSTRAINT FK_TRANX_SALES_ORDER_DETAILS_UNITS_TBL_ON_UNIT FOREIGN KEY (unit_id) REFERENCES units_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;