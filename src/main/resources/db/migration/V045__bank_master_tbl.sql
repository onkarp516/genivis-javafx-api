SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS bank_master_tbl;
CREATE TABLE bank_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   bank_name VARCHAR(255) NULL,
   branch VARCHAR(255) NULL,
   account_number VARCHAR(255) NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_bank_master_tbl PRIMARY KEY (id)
);
SET FOREIGN_KEY_CHECKS = 1;