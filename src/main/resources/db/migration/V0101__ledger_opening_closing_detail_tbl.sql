SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE ledger_opening_closing_detail_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   opening_amount DOUBLE NULL,
   tranx_action VARCHAR(255) NULL,
   closing_amount DOUBLE NULL,
   tranx_date datetime NULL,
   tranx_id BIGINT NULL,
   tranx_type_id BIGINT NULL,
   amount DOUBLE NULL,
   fiscal_year_id BIGINT NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_id BIGINT NULL,
   tranx_code VARCHAR(255) NULL,
   CONSTRAINT pk_ledger_opening_closing_detail_tbl PRIMARY KEY (id)
);

ALTER TABLE ledger_opening_closing_detail_tbl ADD CONSTRAINT FK_LEDGER_OPENING_CLOSING_DETAIL_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE ledger_opening_closing_detail_tbl ADD CONSTRAINT FK_LEDGER_OPENING_CLOSING_DETAIL_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE ledger_opening_closing_detail_tbl ADD CONSTRAINT FK_LEDGER_OPENING_CLOSING_DETAIL_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;