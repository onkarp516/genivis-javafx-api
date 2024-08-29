SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS area_master_tbl;
CREATE TABLE area_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   area_name VARCHAR(255) NULL,
   pincode VARCHAR(255) NULL,
   area_code VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_area_master_tbl PRIMARY KEY (id)
);

ALTER TABLE area_master_tbl ADD CONSTRAINT FK_AREA_MASTER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE area_master_tbl ADD CONSTRAINT FK_AREA_MASTER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;