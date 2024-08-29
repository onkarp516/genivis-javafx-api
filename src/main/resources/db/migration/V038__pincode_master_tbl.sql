SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS pincode_master_tbl;
CREATE TABLE pincode_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   pincode VARCHAR(255) NULL,
   district VARCHAR(255) NULL,
   state VARCHAR(255) NULL,
   state_code VARCHAR(255) NULL,
   area VARCHAR(255) NULL,
   CONSTRAINT pk_pincode_master_tbl PRIMARY KEY (id)
);
SET FOREIGN_KEY_CHECKS = 1;