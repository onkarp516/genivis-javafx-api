SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS product_batchno_tbl;
CREATE TABLE product_batchno_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   batch_no VARCHAR(255) NULL,
   serial_no VARCHAR(255) NULL,
   mrp DOUBLE NULL,
   status BIT(1) NULL,
   qnty INT NULL,
   sales_rate DOUBLE NULL,
   purchase_rate DOUBLE NULL,
   expiry_date date NULL,
   min_ratea DOUBLE NULL,
   min_rateb DOUBLE NULL,
   min_ratec DOUBLE NULL,
   max_discount DOUBLE NULL,
   min_discount DOUBLE NULL,
   min_margin DOUBLE NULL,
   manufacturing_date date NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   product_id BIGINT NULL,
   packaging_id BIGINT NULL,
   unit_id BIGINT NULL,
   flavour_master_id BIGINT NULL,
   brand_id BIGINT NULL,
   group_id BIGINT NULL,
   category_id BIGINT NULL,
   subcategory_id BIGINT NULL,
   opening_qty DOUBLE NULL,
   free_qty DOUBLE NULL,
   costing DOUBLE NULL,
   costing_with_tax DOUBLE NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   supplier_id BIGINT NULL,
   CONSTRAINT pk_product_batchno_tbl PRIMARY KEY (id)
);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_FLAVOUR_MASTER FOREIGN KEY (flavour_master_id) REFERENCES flavour_master_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_GROUP FOREIGN KEY (group_id) REFERENCES group_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_PACKAGING FOREIGN KEY (packaging_id) REFERENCES packing_master_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES sub_category_tbl (id);

ALTER TABLE product_batchno_tbl ADD CONSTRAINT FK_PRODUCT_BATCHNO_TBL_ON_UNIT FOREIGN KEY (unit_id) REFERENCES units_tbl (id);

DROP TABLE if EXISTS product_unit_packing_tbl;
CREATE TABLE product_unit_packing_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   unit_conversion DOUBLE NULL,
   unit_conv_margn DOUBLE NULL,
   purchase_rate DOUBLE NULL,
   mrp DOUBLE NULL,
   is_negative_stocks BIT(1) NULL,
   min_ratea DOUBLE NULL,
   min_rateb DOUBLE NULL,
   min_ratec DOUBLE NULL,
   tax_applicable_date date NULL,
   max_discount DOUBLE NULL,
   min_discount DOUBLE NULL,
   min_margin DOUBLE NULL,
   status BIT(1) NULL,
   product_id BIGINT NULL,
   units_id BIGINT NULL,
   packing_master_id BIGINT NULL,
   flavour_master_id BIGINT NULL,
   brand_id BIGINT NULL,
   group_id BIGINT NULL,
   category_id BIGINT NULL,
   subcategory_id BIGINT NULL,
   hsn_id BIGINT NULL,
   taxmaster_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   min_qty DOUBLE NULL,
   max_qty DOUBLE NULL,
   opening_stocks DOUBLE NULL,
   costing DOUBLE NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   costing_with_tax DOUBLE NULL,
   CONSTRAINT pk_product_unit_packing_tbl PRIMARY KEY (id)
);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_FLAVOUR_MASTER FOREIGN KEY (flavour_master_id) REFERENCES flavour_master_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_GROUP FOREIGN KEY (group_id) REFERENCES group_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_HSN FOREIGN KEY (hsn_id) REFERENCES product_hsn_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_PACKING_MASTER FOREIGN KEY (packing_master_id) REFERENCES packing_master_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES sub_category_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_TAXMASTER FOREIGN KEY (taxmaster_id) REFERENCES tax_master_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_UNITS FOREIGN KEY (units_id) REFERENCES units_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE product_unit_packing_tbl ADD CONSTRAINT FK_PRODUCT_UNIT_PACKING_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

DROP TABLE if EXISTS product_opening_stocks_tbl;
CREATE TABLE product_opening_stocks_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   opening_qty DOUBLE NULL,
   opening_valuation DOUBLE NULL,
   product_id BIGINT NOT NULL,
   fiscal_year_id BIGINT NULL,
   units_id BIGINT NULL,
   packing_master_id BIGINT NULL,
   flavour_master_id BIGINT NULL,
   batch_id BIGINT NULL,
   brand_id BIGINT NULL,
   group_id BIGINT NULL,
   category_id BIGINT NULL,
   subcategory_id BIGINT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   status BIT(1) NULL,
   free_opening_qty DOUBLE NULL,
   costing DOUBLE NULL,
   opening_stocks DOUBLE NULL,
   mrp DOUBLE NULL,
   purchase_rate DOUBLE NULL,
   sales_rate DOUBLE NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   expiry_date date NULL,
   manufacturing_date date NULL,
   costing_with_tax DOUBLE NULL,
   serial_no VARCHAR(255) NULL,
   subgroup_id BIGINT NULL,
   CONSTRAINT pk_product_opening_stocks_tbl PRIMARY KEY (id)
);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_BATCH FOREIGN KEY (batch_id) REFERENCES product_batchno_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_FLAVOUR_MASTER FOREIGN KEY (flavour_master_id) REFERENCES flavour_master_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_GROUP FOREIGN KEY (group_id) REFERENCES group_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_PACKING_MASTER FOREIGN KEY (packing_master_id) REFERENCES packing_master_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES sub_category_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_UNITS FOREIGN KEY (units_id) REFERENCES units_tbl (id);

ALTER TABLE product_opening_stocks_tbl ADD CONSTRAINT FK_PRODUCT_OPENING_STOCKS_TBL_ON_SUBGROUP FOREIGN KEY (subgroup_id) REFERENCES subgroup_tbl (id);

DROP TABLE if EXISTS product_tax_date_master_tbl;
CREATE TABLE product_tax_date_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   applicable_date date NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   product_id BIGINT NULL,
   outlet_id BIGINT NULL,
   product_hsn_id BIGINT NULL,
   tax_master_id BIGINT NULL,
   batch_id BIGINT NULL,
   units_id BIGINT NULL,
   packing_master_id BIGINT NULL,
   flavour_master_id BIGINT NULL,
   brand_id BIGINT NULL,
   group_id BIGINT NULL,
   category_id BIGINT NULL,
   subcategory_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   unique_code DOUBLE NULL,
   subgroup_id BIGINT NULL,
   CONSTRAINT pk_product_tax_date_master_tbl PRIMARY KEY (id)
);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_BATCH FOREIGN KEY (batch_id) REFERENCES product_batchno_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_FLAVOUR_MASTER FOREIGN KEY (flavour_master_id) REFERENCES flavour_master_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_GROUP FOREIGN KEY (group_id) REFERENCES group_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_PACKING_MASTER FOREIGN KEY (packing_master_id) REFERENCES packing_master_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_PRODUCT_HSN FOREIGN KEY (product_hsn_id) REFERENCES product_hsn_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES sub_category_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_TAX_MASTER FOREIGN KEY (tax_master_id) REFERENCES tax_master_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_UNITS FOREIGN KEY (units_id) REFERENCES units_tbl (id);

ALTER TABLE product_tax_date_master_tbl ADD CONSTRAINT FK_PRODUCT_TAX_DATE_MASTER_TBL_ON_SUBGROUP FOREIGN KEY (subgroup_id) REFERENCES subgroup_tbl (id);


DROP TABLE if EXISTS product_barcode_tbl;
CREATE TABLE product_barcode_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   barcode_unique_code VARCHAR(255) NULL,
   batch_no VARCHAR(255) NULL,
   serial_no VARCHAR(255) NULL,
   mrp DOUBLE NULL,
   tranx_date date NULL,
   enable BIT(1) NULL,
   status BIT(1) NULL,
   print_qnt INT NULL,
   qnty INT NULL,
   is_auto BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   product_id BIGINT NULL,
   packaging_id BIGINT NULL,
   unit_id BIGINT NULL,
   flavour_master_id BIGINT NULL,
   batch_id BIGINT NULL,
   brand_id BIGINT NULL,
   group_id BIGINT NULL,
   category_id BIGINT NULL,
   subcategory_id BIGINT NULL,
   tranx_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   transaction_id BIGINT NULL,
   company_barcode VARCHAR(255) NULL,
   CONSTRAINT pk_product_barcode_tbl PRIMARY KEY (id)
);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_BATCH FOREIGN KEY (batch_id) REFERENCES product_batchno_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_FLAVOUR_MASTER FOREIGN KEY (flavour_master_id) REFERENCES flavour_master_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_GROUP FOREIGN KEY (group_id) REFERENCES group_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_PACKAGING FOREIGN KEY (packaging_id) REFERENCES packing_master_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES sub_category_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_TRANX FOREIGN KEY (tranx_id) REFERENCES transaction_type_master_tbl (id);

ALTER TABLE product_barcode_tbl ADD CONSTRAINT FK_PRODUCT_BARCODE_TBL_ON_UNIT FOREIGN KEY (unit_id) REFERENCES units_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;
