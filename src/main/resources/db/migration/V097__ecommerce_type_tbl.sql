SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS ecommerce_type_tbl;
CREATE TABLE ecommerce_type_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   type VARCHAR(255) NULL,
   status BIT(1) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   outlet_id BIGINT NULL,
   branch_id BIGINT NULL,
   CONSTRAINT pk_ecommerce_type_tbl PRIMARY KEY (id)
);

SET FOREIGN_KEY_CHECKS = 1;
