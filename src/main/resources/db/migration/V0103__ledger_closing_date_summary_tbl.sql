CREATE TABLE ledger_closing_date_summary_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   closing_date date NULL,
   opening_amount DOUBLE NULL,
   total_amount DOUBLE NULL,
   closing_amount DOUBLE NULL,
   ledger_type VARCHAR(255) NULL,
   fiscal_year_id BIGINT NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   ledger_id BIGINT NULL,
   CONSTRAINT pk_ledger_closing_date_summary_tbl PRIMARY KEY (id)
);

ALTER TABLE ledger_closing_date_summary_tbl ADD CONSTRAINT FK_LEDGER_CLOSING_DATE_SUMMARY_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE ledger_closing_date_summary_tbl ADD CONSTRAINT FK_LEDGER_CLOSING_DATE_SUMMARY_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE ledger_closing_date_summary_tbl ADD CONSTRAINT FK_LEDGER_CLOSING_DATE_SUMMARY_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);