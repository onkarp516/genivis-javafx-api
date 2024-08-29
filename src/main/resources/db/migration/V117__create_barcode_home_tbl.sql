CREATE TABLE barcode_home_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   barcode_home_path VARCHAR(255) NULL,
   prn_file_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   modify_date date NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   CONSTRAINT pk_barcode_home_tbl PRIMARY KEY (id)
);