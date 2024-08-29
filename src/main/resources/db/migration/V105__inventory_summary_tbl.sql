SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS stock_opening_closing_snap_tbl;
CREATE TABLE stock_opening_closing_snap_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   opening_stock DOUBLE NULL,
   tranx_action VARCHAR(255) NULL,
   closing_stock DOUBLE NULL,
   tranx_date datetime NULL,
   tranx_id BIGINT NULL,
   tranx_type_id BIGINT NULL,
   qty DOUBLE NULL,
   financial_year BIGINT NULL,
   status BIT(1) NULL,
   valuation DOUBLE NULL,
   avg_valuation DOUBLE NULL,
   pur_price DOUBLE NULL,
   sales_price DOUBLE NULL,
   batch_id BIGINT NULL,
   package_id BIGINT NULL,
   unit_id BIGINT NULL,
   serial_num VARCHAR(255) NULL,
   levelaid BIGINT NULL,
   levelbid BIGINT NULL,
   levelcid BIGINT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   product_id BIGINT NULL,
   tranx_code VARCHAR(255) NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   created_by BIGINT NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_stock_opening_closing_snap_tbl PRIMARY KEY (id)
);

ALTER TABLE stock_opening_closing_snap_tbl ADD CONSTRAINT FK_STOCK_OPENING_CLOSING_SNAP_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE stock_opening_closing_snap_tbl ADD CONSTRAINT FK_STOCK_OPENING_CLOSING_SNAP_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE stock_opening_closing_snap_tbl ADD CONSTRAINT FK_STOCK_OPENING_CLOSING_SNAP_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);
DROP TABLE if EXISTS stock_opening_closing_summary_tbl;
CREATE TABLE stock_opening_closing_summary_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   product_id BIGINT NULL,
   closing_stock DOUBLE NULL,
   opening_stock DOUBLE NULL,
   valuation DOUBLE NULL,
   avg_valuation DOUBLE NULL,
   pur_price DOUBLE NULL,
   sales_price DOUBLE NULL,
   batch_id BIGINT NULL,
   qty DOUBLE NULL,
   in_total DOUBLE NULL,
   out_total DOUBLE NULL,
   package_id BIGINT NULL,
   units_id BIGINT NULL,
   tranx_date datetime NULL,
   status BIT(1) NULL,
   fiscal_year_id BIGINT NULL,
   tranx_code VARCHAR(255) NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   created_by BIGINT NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_stock_opening_closing_summary_tbl PRIMARY KEY (id)
);

ALTER TABLE stock_opening_closing_summary_tbl ADD CONSTRAINT FK_STOCK_OPENING_CLOSING_SUMMARY_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE stock_opening_closing_summary_tbl ADD CONSTRAINT FK_STOCK_OPENING_CLOSING_SUMMARY_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE stock_opening_closing_summary_tbl ADD CONSTRAINT FK_STOCK_OPENING_CLOSING_SUMMARY_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;