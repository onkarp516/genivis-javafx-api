SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_purchase_order_tbl;
CREATE TABLE tranx_purchase_order_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   sundry_creditors_id BIGINT NULL,
   purchase_account_ledger_id BIGINT NULL,
   purchase_roundoff_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   transaction_status_id BIGINT NOT NULL,
   pur_ord_srno BIGINT NULL,
   vendor_invoice_no VARCHAR(255) NULL,
   order_reference VARCHAR(255) NULL,
   transaction_date date NULL,
   invoice_date datetime NULL,
   transport_name VARCHAR(255) NULL,
   reference VARCHAR(255) NULL,
   round_off DOUBLE NULL,
   total_base_amount DOUBLE NULL,
   total_amount DOUBLE NULL,
   totalcgst DOUBLE NULL,
   totalqty BIGINT NULL,
   totalsgst DOUBLE NULL,
   totaligst DOUBLE NULL,
   taxable_amount DOUBLE NULL,
   tcs DOUBLE NULL,
   status BIT(1) NULL,
   financial_year VARCHAR(255) NULL,
   narration VARCHAR(255) NULL,
   operations VARCHAR(255) NULL,
   is_challan_converted BIT(1) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   ledger_id BIGINT NULL,
   ledger_amt DOUBLE NULL,
   free_qty DOUBLE NULL,
   purchase_discount_amount DOUBLE NULL,
   purchase_discount_per DOUBLE NULL,
   total_purchase_discount_amt DOUBLE NULL,
   gross_amount DOUBLE NULL,
   total_tax DOUBLE NULL,
   gst_number VARCHAR(255) NULL,
   CONSTRAINT pk_tranx_purchase_order_tbl PRIMARY KEY (id)
);


ALTER TABLE tranx_purchase_order_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_purchase_order_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE tranx_purchase_order_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_order_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_purchase_order_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_TBL_ON_PURCHASE_ACCOUNT_LEDGER FOREIGN KEY (purchase_account_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_order_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_TBL_ON_PURCHASE_ROUNDOFF FOREIGN KEY (purchase_roundoff_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_order_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_TBL_ON_SUNDRY_CREDITORS FOREIGN KEY (sundry_creditors_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_order_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_TBL_ON_TRANSACTION_STATUS FOREIGN KEY (transaction_status_id) REFERENCES transaction_status_tbl (id);

DROP TABLE if EXISTS tranx_purchase_order_details_tbl;
CREATE TABLE tranx_purchase_order_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   tranx_pur_order_id BIGINT NULL,
   product_id BIGINT NULL,
   packaging_id BIGINT NULL,
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
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_tranx_purchase_order_details_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_purchase_order_details_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_TBL_ON_PACKAGING FOREIGN KEY (packaging_id) REFERENCES packing_master_tbl (id);

ALTER TABLE tranx_purchase_order_details_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_purchase_order_details_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_TBL_ON_TRANX_PUR_ORDER FOREIGN KEY (tranx_pur_order_id) REFERENCES tranx_purchase_order_tbl (id);

DROP TABLE if EXISTS tranx_purchase_order_duties_taxes_tbl;
CREATE TABLE tranx_purchase_order_duties_taxes_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  tranx_pur_order_id BIGINT NOT NULL,
  sundry_creditors_id BIGINT NOT NULL,
  duties_taxes_ledger_id BIGINT NOT NULL,
  intra BIT(1) NULL,
  amount DOUBLE NULL,
  status BIT(1) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  updated_at datetime NULL,
  updated_by BIGINT NULL,
  CONSTRAINT pk_tranx_purchase_order_duties_taxes_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_purchase_order_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DUTIES_TAXES_TBL_ON_DUTIES_TAXES_LEDGER FOREIGN KEY (duties_taxes_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_order_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DUTIES_TAXES_TBL_ON_SUNDRY_CREDITORS FOREIGN KEY (sundry_creditors_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_purchase_order_duties_taxes_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DUTIES_TAXES_TBL_ON_TRANX_PUR_ORDER FOREIGN KEY (tranx_pur_order_id) REFERENCES tranx_purchase_order_tbl (id);

DROP TABLE if EXISTS tranx_purchase_order_details_units_tbl;
CREATE TABLE tranx_purchase_order_details_units_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   purchase_order_id BIGINT NULL,
   purchase_order_details_id BIGINT NULL,
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
   gross_amt1 DOUBLE NULL,
   invoice_dis_amt DOUBLE NULL,
   taxable_amount DOUBLE NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   transaction_status BIGINT NULL,
   CONSTRAINT pk_tranx_purchase_order_details_units_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANXPURCHASEORDERDETAILSUNITSTBL_ON_PURCHASEORDERDETAILS FOREIGN KEY (purchase_order_details_id) REFERENCES tranx_purchase_order_details_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_FLAVOUR_MASTER FOREIGN KEY (flavour_master_id) REFERENCES flavour_master_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_GROUP FOREIGN KEY (group_id) REFERENCES group_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_PACKAGING FOREIGN KEY (packaging_id) REFERENCES packing_master_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_PURCHASE_ORDER FOREIGN KEY (purchase_order_id) REFERENCES tranx_purchase_order_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES sub_category_tbl (id);

ALTER TABLE tranx_purchase_order_details_units_tbl ADD CONSTRAINT FK_TRANX_PURCHASE_ORDER_DETAILS_UNITS_TBL_ON_UNIT FOREIGN KEY (unit_id) REFERENCES units_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;

