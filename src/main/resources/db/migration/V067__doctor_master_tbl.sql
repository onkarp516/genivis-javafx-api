SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS doctor_master_tbl;
CREATE TABLE doctor_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   doctor_name VARCHAR(255) NULL,
   specialization VARCHAR(255) NULL,
   hospital_name VARCHAR(255) NULL,
   hospital_address VARCHAR(255) NULL,
   mobile_number VARCHAR(255) NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_doctor_master_tbl PRIMARY KEY (id)
);
SET FOREIGN_KEY_CHECKS = 1;