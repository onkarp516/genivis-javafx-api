SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS ledger_paymentmode_details_tbl;
CREATE TABLE ledger_paymentmode_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   ledger_id BIGINT NULL,
   payment_mode_master_id BIGINT NULL,
   status BIT(1) NULL,
   created_by BIGINT NULL,
   updated_by BIGINT NULL,
   created_at date NULL,
   updated_at datetime NULL,
   CONSTRAINT pk_ledger_paymentmode_details_tbl PRIMARY KEY (id)
);


SET FOREIGN_KEY_CHECKS = 1;

