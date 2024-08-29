SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS filters_masters_tbl;
CREATE TABLE filters_masters_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   filter_name VARCHAR(255) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   CONSTRAINT pk_filters_masters_tbl PRIMARY KEY (id)
);

ALTER TABLE filters_masters_tbl ADD CONSTRAINT FK_FILTERS_MASTERS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE filters_masters_tbl ADD CONSTRAINT FK_FILTERS_MASTERS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS sub_filters_masters_tbl;
CREATE TABLE sub_filters_masters_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   filter_id BIGINT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   sub_filter_name VARCHAR(255) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_sub_filters_masters_tbl PRIMARY KEY (id)
);

ALTER TABLE sub_filters_masters_tbl ADD CONSTRAINT FK_SUB_FILTERS_MASTERS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE sub_filters_masters_tbl ADD CONSTRAINT FK_SUB_FILTERS_MASTERS_TBL_ON_FILTER FOREIGN KEY (filter_id) REFERENCES filters_masters_tbl (id);

ALTER TABLE sub_filters_masters_tbl ADD CONSTRAINT FK_SUB_FILTERS_MASTERS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);
DROP TABLE if EXISTS brand_tbl;
CREATE TABLE brand_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   brand_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_brand_tbl PRIMARY KEY (id)
);

ALTER TABLE brand_tbl ADD CONSTRAINT FK_BRAND_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE brand_tbl ADD CONSTRAINT FK_BRAND_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);
DROP TABLE if EXISTS group_tbl;
CREATE TABLE group_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   group_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_group_tbl PRIMARY KEY (id)
);

ALTER TABLE group_tbl ADD CONSTRAINT FK_GROUP_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE group_tbl ADD CONSTRAINT FK_GROUP_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS category_tbl;
CREATE TABLE category_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   category_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_category_tbl PRIMARY KEY (id)
);

ALTER TABLE category_tbl ADD CONSTRAINT FK_CATEGORY_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE category_tbl ADD CONSTRAINT FK_CATEGORY_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS sub_category_tbl;
CREATE TABLE sub_category_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   subcategory_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_sub_category_tbl PRIMARY KEY (id)
);
ALTER TABLE sub_category_tbl ADD CONSTRAINT FK_SUB_CATEGORY_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE sub_category_tbl ADD CONSTRAINT FK_SUB_CATEGORY_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS packing_master_tbl;
CREATE TABLE packing_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   pack_name VARCHAR(255) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   status BIT(1) NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_packing_master_tbl PRIMARY KEY (id)
);

ALTER TABLE packing_master_tbl ADD CONSTRAINT FK_PACKING_MASTER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE packing_master_tbl ADD CONSTRAINT FK_PACKING_MASTER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);
DROP TABLE if EXISTS units_tbl;
CREATE TABLE units_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   unit_name VARCHAR(255) NULL,
   unit_code VARCHAR(255) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_units_tbl PRIMARY KEY (id)
);
ALTER TABLE units_tbl ADD CONSTRAINT FK_UNITS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE units_tbl ADD CONSTRAINT FK_UNITS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS product_hsn_tbl;
CREATE TABLE product_hsn_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  hsn_number VARCHAR(255) NULL,
  description VARCHAR(255) NULL,
  igst DOUBLE NULL,
  cgst DOUBLE NULL,
  sgst DOUBLE NULL,
  type VARCHAR(255) NULL,
  branch_id BIGINT NULL,
  outlet_id BIGINT NOT NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  updated_at datetime NULL,
  updated_by BIGINT NULL,
  status BIT(1) NULL,
  CONSTRAINT pk_producthsn_tbl PRIMARY KEY (id)
);

ALTER TABLE product_hsn_tbl ADD CONSTRAINT FK_PRODUCTHSN_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE product_hsn_tbl ADD CONSTRAINT FK_PRODUCTHSN_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS tax_master_tbl;
CREATE TABLE tax_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  gst_per VARCHAR(255) NULL,
  cgst DOUBLE NULL,
  sgst DOUBLE NULL,
  igst DOUBLE NULL,
  sratio DOUBLE NULL,
  applicable_date date NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  status BIT(1) NULL,
  branch_id BIGINT NULL,
  outlet_id BIGINT NULL,
  updated_at datetime NULL,
  updated_by BIGINT NULL,
  CONSTRAINT pk_tax_master_tbl PRIMARY KEY (id)
);

ALTER TABLE tax_master_tbl ADD CONSTRAINT FK_TAX_MASTER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tax_master_tbl ADD CONSTRAINT FK_TAX_MASTER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS product_tbl;
CREATE TABLE product_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   product_name VARCHAR(255) NULL,
   product_code VARCHAR(255) NULL,
   status BIT(1) NULL,
   description VARCHAR(255) NULL,
   alias VARCHAR(255) NULL,
   is_warranty_applicable BIT(1) NULL,
   warranty_days INT NULL,
   is_serial_number BIT(1) NULL,
   is_batch_number BIT(1) NULL,
   is_draft BIT(1) NULL,
   is_inventory BIT(1) NULL,
   is_brand BIT(1) NULL,
   is_group BIT(1) NULL,
   is_category BIT(1) NULL,
   is_sub_category BIT(1) NULL,
   is_package BIT(1) NULL,
   created_by BIGINT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   brand_id BIGINT NULL,
   group_id BIGINT NULL,
   category_id BIGINT NULL,
   subcategory_id BIGINT NULL,
   hsn_id BIGINT NULL,
   taxmaster_id BIGINT NULL,
   shelf_id VARCHAR(255) NULL,
   tax_type VARCHAR(255) NULL,
   applicable_date date NULL,
   igst DOUBLE NULL,
   cgst DOUBLE NULL,
   sgst DOUBLE NULL,
   barcode_sales_qty DOUBLE NULL,
   purchase_rate DOUBLE NULL,
   packing_master_id BIGINT NULL,
   weight DOUBLE NULL,
   weight_unit VARCHAR(255) NULL,
   discount_in_per DOUBLE NULL,
   margin_per DOUBLE NULL,
   barcode_no VARCHAR(255) NULL,
   min_stock DOUBLE NULL,
   max_stock DOUBLE NULL,
   subgroup_id BIGINT NULL,
   is_delete BIT(1) NULL,
   drug_type VARCHAR(255) NULL,
   product_type VARCHAR(255) NULL,
   gv_of_products VARCHAR(255) NULL,
   is_commision BIT(1) NULL,
   isgvproducts BIT(1) NULL,
   CONSTRAINT pk_product_tbl PRIMARY KEY (id)
);

ALTER TABLE product_tbl ADD CONSTRAINT FK_PRODUCT_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE product_tbl ADD CONSTRAINT FK_PRODUCT_TBL_ON_BRAND FOREIGN KEY (brand_id) REFERENCES brand_tbl (id);

ALTER TABLE product_tbl ADD CONSTRAINT FK_PRODUCT_TBL_ON_CATEGORY FOREIGN KEY (category_id) REFERENCES category_tbl (id);

ALTER TABLE product_tbl ADD CONSTRAINT FK_PRODUCT_TBL_ON_GROUP FOREIGN KEY (group_id) REFERENCES group_tbl (id);

ALTER TABLE product_tbl ADD CONSTRAINT FK_PRODUCT_TBL_ON_HSN FOREIGN KEY (hsn_id) REFERENCES product_hsn_tbl (id);

ALTER TABLE product_tbl ADD CONSTRAINT FK_PRODUCT_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE product_tbl ADD CONSTRAINT FK_PRODUCT_TBL_ON_SUBCATEGORY FOREIGN KEY (subcategory_id) REFERENCES sub_category_tbl (id);

ALTER TABLE product_tbl ADD CONSTRAINT FK_PRODUCT_TBL_ON_TAXMASTER FOREIGN KEY (taxmaster_id) REFERENCES tax_master_tbl (id);

ALTER TABLE product_tbl ADD CONSTRAINT FK_PRODUCT_TBL_ON_PACKING_MASTER FOREIGN KEY (packing_master_id) REFERENCES packing_master_tbl (id);

ALTER TABLE product_tbl ADD CONSTRAINT FK_PRODUCT_TBL_ON_SUBGROUP FOREIGN KEY (subgroup_id) REFERENCES subgroup_tbl (id);


DROP TABLE if EXISTS product_filter_mapping_tbl;
CREATE TABLE product_filter_mapping_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   product_id BIGINT NOT NULL,
   branch_id BIGINT NULL,
   filter_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   status BIT(1) NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_product_filter_mapping_tbl PRIMARY KEY (id)
);

ALTER TABLE product_filter_mapping_tbl ADD CONSTRAINT FK_PRODUCT_FILTER_MAPPING_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE product_filter_mapping_tbl ADD CONSTRAINT FK_PRODUCT_FILTER_MAPPING_TBL_ON_FILTER FOREIGN KEY (filter_id) REFERENCES filters_masters_tbl (id);

ALTER TABLE product_filter_mapping_tbl ADD CONSTRAINT FK_PRODUCT_FILTER_MAPPING_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE product_filter_mapping_tbl ADD CONSTRAINT FK_PRODUCT_FILTER_MAPPING_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);


DROP TABLE if EXISTS product_image_master_tbl;
CREATE TABLE product_image_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   product_id BIGINT NULL,
   image_path VARCHAR(255) NULL,
   CONSTRAINT pk_product_image_master_tbl PRIMARY KEY (id)
);
ALTER TABLE product_image_master_tbl ADD CONSTRAINT FK_PRODUCT_IMAGE_MASTER_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);
DROP TABLE if EXISTS level_a_tbl;
CREATE TABLE level_a_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   level_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_level_a_tbl PRIMARY KEY (id)
);

ALTER TABLE level_a_tbl ADD CONSTRAINT FK_LEVEL_A_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE level_a_tbl ADD CONSTRAINT FK_LEVEL_A_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS level_b_tbl;
CREATE TABLE level_b_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   level_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_level_b_tbl PRIMARY KEY (id)
);

ALTER TABLE level_b_tbl ADD CONSTRAINT FK_LEVEL_B_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE level_b_tbl ADD CONSTRAINT FK_LEVEL_B_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS level_c_tbl;
CREATE TABLE level_c_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   level_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_level_c_tbl PRIMARY KEY (id)
);

ALTER TABLE level_c_tbl ADD CONSTRAINT FK_LEVEL_C_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE level_c_tbl ADD CONSTRAINT FK_LEVEL_C_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

DROP TABLE if EXISTS drug_type_tbl;
CREATE TABLE drug_type_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   drug_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_drug_type_tbl PRIMARY KEY (id)
);
INSERT INTO drug_type_tbl (
  drug_name,
  status
) VALUES
  (
    'H',
    1
  ),
  (
      'H1',
      1
    ),
  (
    'Narcotic',
    1
  );

  DROP TABLE if EXISTS product_content_master_tbl;
  CREATE TABLE product_content_master_tbl (
    id BIGINT AUTO_INCREMENT NOT NULL,
     content_type VARCHAR(255) NULL,
     content_power VARCHAR(255) NULL,
     content_package VARCHAR(255) NULL,
     status BIT(1) NULL,
     product_id BIGINT NOT NULL,
     CONSTRAINT pk_product_content_master_tbl PRIMARY KEY (id)
  );

  ALTER TABLE product_content_master_tbl ADD CONSTRAINT FK_PRODUCT_CONTENT_MASTER_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;

