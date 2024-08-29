SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS content_master_dose_tbl;

CREATE TABLE content_master_dose_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   content_name_dose VARCHAR(255) NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_content_master_dose_tbl PRIMARY KEY (id)
);

SET FOREIGN_KEY_CHECKS = 1;