SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS subgroup_tbl;
CREATE TABLE subgroup_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   subgroup_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_subgroup_tbl PRIMARY KEY (id)
);

ALTER TABLE subgroup_tbl ADD CONSTRAINT FK_SUBGROUP_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE subgroup_tbl ADD CONSTRAINT FK_SUBGROUP_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);
DROP TABLE if EXISTS flavour_master_tbl;
CREATE TABLE flavour_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   flavour_name VARCHAR(255) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_flavour_master_tbl PRIMARY KEY (id)
);
ALTER TABLE flavour_master_tbl ADD CONSTRAINT FK_FLAVOUR_MASTER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE flavour_master_tbl ADD CONSTRAINT FK_FLAVOUR_MASTER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;