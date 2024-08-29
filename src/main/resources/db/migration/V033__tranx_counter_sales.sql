SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_counter_sales_tbl;
CREATE TABLE tranx_counter_sales_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   counter_sale_sr_no BIGINT NULL,
   counter_sale_no VARCHAR(255) NULL,
   transaction_date datetime NULL,
   customer_name VARCHAR(255) NULL,
   mobile_number BIGINT NULL,
   total_bill DOUBLE NULL,
   total_base_amt DOUBLE NULL,
   taxable_amt DOUBLE NULL,
   roundoff DOUBLE NULL,
   status BIT(1) NULL,
   is_bill_converted BIT(1) NULL,
   created_by BIGINT NULL,
   financial_year VARCHAR(255) NULL,
   total_discount DOUBLE NULL,
   narrations VARCHAR(255) NULL,
   free_qty DOUBLE NULL,
   totalqty DOUBLE NULL,
   totalcgst DOUBLE NULL,
   totalsgst DOUBLE NULL,
   totaligst DOUBLE NULL,
   payment_mode VARCHAR(255) NULL,
   payment_amount DOUBLE NULL,
   cash DOUBLE NULL,
   digital DOUBLE NULL,
   card_payment DOUBLE NULL,
   advanced_amount DOUBLE NULL,
   operations VARCHAR(255) NULL,
   created_at datetime NULL,
   updated_by BIGINT NULL,
   counter_sales_date date NULL,
   discount_amt DOUBLE NULL,
   transaction_status BIGINT NULL,
   CONSTRAINT pk_tranx_counter_sales_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_counter_sales_tbl ADD CONSTRAINT FK_TRANX_COUNTER_SALES_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_counter_sales_tbl ADD CONSTRAINT FK_TRANX_COUNTER_SALES_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE tranx_counter_sales_tbl ADD CONSTRAINT FK_TRANX_COUNTER_SALES_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);
DROP TABLE if EXISTS tranx_counter_sales_details_units_tbl;
CREATE TABLE tranx_counter_sales_details_units_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   counter_sales_id BIGINT NULL,
   product_id BIGINT NULL,
   unit_id BIGINT NULL,
   batch_id BIGINT NULL,
   unit_conversions DOUBLE NULL,
   qty DOUBLE NULL,
   rate DOUBLE NULL,
   base_amt DOUBLE NULL,
   net_amount DOUBLE NULL,
   discount_amount DOUBLE NULL,
   discount_per DOUBLE NULL,
   discountbin_per DOUBLE NULL,
   row_discountamt DOUBLE NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   status BIT(1) NULL,
   free_qty DOUBLE NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   CONSTRAINT pk_tranx_counter_sales_details_units_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_counter_sales_details_units_tbl ADD CONSTRAINT FK_TRANX_COUNTER_SALES_DETAILS_UNITS_TBL_ON_BATCH FOREIGN KEY (batch_id) REFERENCES product_batchno_tbl (id);

ALTER TABLE tranx_counter_sales_details_units_tbl ADD CONSTRAINT FK_TRANX_COUNTER_SALES_DETAILS_UNITS_TBL_ON_COUNTER_SALES FOREIGN KEY (counter_sales_id) REFERENCES tranx_counter_sales_tbl (id);

ALTER TABLE tranx_counter_sales_details_units_tbl ADD CONSTRAINT FK_TRANX_COUNTER_SALES_DETAILS_UNITS_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE tranx_counter_sales_details_units_tbl ADD CONSTRAINT FK_TRANX_COUNTER_SALES_DETAILS_UNITS_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE tranx_counter_sales_details_units_tbl ADD CONSTRAINT FK_TRANX_COUNTER_SALES_DETAILS_UNITS_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

ALTER TABLE tranx_counter_sales_details_units_tbl ADD CONSTRAINT FK_TRANX_COUNTER_SALES_DETAILS_UNITS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_counter_sales_details_units_tbl ADD CONSTRAINT FK_TRANX_COUNTER_SALES_DETAILS_UNITS_TBL_ON_UNIT FOREIGN KEY (unit_id) REFERENCES units_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;
