SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS salesman_master_tbl;
CREATE TABLE salesman_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   first_name VARCHAR(255) NULL,
   last_name VARCHAR(255) NULL,
   middle_name VARCHAR(255) NULL,
   mobile_number VARCHAR(255) NULL,
   pincode VARCHAR(255) NULL,
   address VARCHAR(255) NULL,
   dob date NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   CONSTRAINT pk_salesman_master_tbl PRIMARY KEY (id)
);

ALTER TABLE salesman_master_tbl ADD CONSTRAINT FK_SALESMAN_MASTER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE salesman_master_tbl ADD CONSTRAINT FK_SALESMAN_MASTER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;