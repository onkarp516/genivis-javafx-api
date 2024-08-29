SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS ledger_license_tbl;
CREATE TABLE ledger_license_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   license_num VARCHAR(255) NULL,
   license_exp date NULL,
   slug_name VARCHAR(255) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   status BIT(1) NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   ledger_id BIGINT NULL,
   CONSTRAINT pk_ledger_license_tbl PRIMARY KEY (id)
);

ALTER TABLE ledger_license_tbl ADD CONSTRAINT FK_LEDGER_LICENSE_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;