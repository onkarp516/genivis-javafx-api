SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS inventory_details_postings_tbl;
CREATE TABLE inventory_details_postings_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   tranx_action VARCHAR(255) NULL,
   tranx_date datetime NULL,
   tranx_id BIGINT NULL,
   qty DOUBLE NULL,
   status BIT(1) NULL,
   valuation DOUBLE NULL,
   avg_valuation DOUBLE NULL,
   pur_price DOUBLE NULL,
   sales_price DOUBLE NULL,
   unique_batch_no VARCHAR(255) NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   created_by BIGINT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   product_id BIGINT NULL,
   transaction_type_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   packaging_id BIGINT NULL,
   flavour_master_id BIGINT NULL,
   units_id BIGINT NULL,
   brand_id BIGINT NULL,
   group_id BIGINT NULL,
   category_id BIGINT NULL,
   subcategory_id BIGINT NULL,
   batch_id BIGINT NULL,
   level_a_id BIGINT NULL,
   level_b_id BIGINT NULL,
   level_c_id BIGINT NULL,
   serial_no VARCHAR(255) NULL,
   CONSTRAINT pk_inventory_details_postings_tbl PRIMARY KEY (id)
);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_BATCH FOREIGN KEY (batch_id) REFERENCES product_batchno_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_FLAVOUR_MASTER FOREIGN KEY (flavour_master_id) REFERENCES flavour_master_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_GROUP FOREIGN KEY (group_id) REFERENCES group_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_LEVEL_A FOREIGN KEY (level_a_id) REFERENCES level_a_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_LEVEL_B FOREIGN KEY (level_b_id) REFERENCES level_b_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_LEVEL_C FOREIGN KEY (level_c_id) REFERENCES level_c_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_PACKAGING FOREIGN KEY (packaging_id) REFERENCES packing_master_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES sub_category_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_TRANSACTION_TYPE FOREIGN KEY (transaction_type_id) REFERENCES transaction_type_master_tbl (id);

ALTER TABLE inventory_details_postings_tbl ADD CONSTRAINT FK_INVENTORY_DETAILS_POSTINGS_TBL_ON_UNITS FOREIGN KEY (units_id) REFERENCES units_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;