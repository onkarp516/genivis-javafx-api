SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_debit_note_new_reference_tbl;
CREATE TABLE tranx_debit_note_new_reference_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   sundry_creditor_id BIGINT NULL,
   tranx_pur_return_invoice_id BIGINT NULL,
   transaction_status_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   srno BIGINT NULL,
   debitnote_new_reference_no VARCHAR(255) NULL,
   round_off DOUBLE NULL,
   total_base_amount DOUBLE NULL,
   total_amount DOUBLE NULL,
   taxable_amount DOUBLE NULL,
   totalgst DOUBLE NULL,
   purchase_discount_amount DOUBLE NULL,
   purchase_discount_per DOUBLE NULL,
   total_purchase_discount_amt DOUBLE NULL,
   additional_charges_total DOUBLE NULL,
   financial_year VARCHAR(255) NULL,
   source VARCHAR(255) NULL,
   transcation_date datetime NULL,
   narrations VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NOT NULL,
   adjustment_status VARCHAR(255) NULL,
   balance DOUBLE NULL,
   purchase_invoice_id BIGINT NULL,
   purchase_challan_id BIGINT NULL,
   payment_id BIGINT NULL,
   adjusted_id BIGINT NULL,
   is_selected BIT(1) NULL,
   CONSTRAINT pk_tranx_debit_note_new_reference_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_debit_note_new_reference_tbl ADD CONSTRAINT FK_TRANXDEBITNOTENEWREFERENCETBL_ON_TRANXPURRETURNINVOICE FOREIGN KEY (tranx_pur_return_invoice_id) REFERENCES tranx_pur_return_invoice_tbl (id);

ALTER TABLE tranx_debit_note_new_reference_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_NEW_REFERENCE_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_debit_note_new_reference_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_NEW_REFERENCE_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE tranx_debit_note_new_reference_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_NEW_REFERENCE_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_debit_note_new_reference_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_NEW_REFERENCE_TBL_ON_SUNDRY_CREDITOR FOREIGN KEY (sundry_creditor_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_debit_note_new_reference_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_NEW_REFERENCE_TBL_ON_TRANSACTION_STATUS FOREIGN KEY (transaction_status_id) REFERENCES transaction_status_tbl (id);

DROP TABLE if EXISTS tranx_debit_note_details_tbl;
CREATE TABLE tranx_debit_note_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   sundry_creditor_id BIGINT NULL,
   transaction_status_id BIGINT NULL,
   tranx_debitnote_master_id BIGINT NULL,
   ledger_id BIGINT NULL,
   type VARCHAR(255) NULL,
   ledger_type VARCHAR(255) NULL,
   total_amount DOUBLE NULL,
   balance DOUBLE NOT NULL,
   adjusted_id BIGINT NULL,
   adjusted_source VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NOT NULL,
   adjustment_status VARCHAR(255) NULL,
   operations VARCHAR(255) NULL,
   paid_amt DOUBLE NULL,
   source VARCHAR(255) NULL,
   dr DOUBLE NOT NULL,
   cr DOUBLE NOT NULL,
   payment_method VARCHAR(255) NULL,
   payment_tranx_no VARCHAR(255) NULL,
   transaction_date datetime NULL,
   payable_amt DOUBLE NULL,
   selected_amt DOUBLE NULL,
   remaining_amt DOUBLE NULL,
   is_advance BIT(1) NULL,
   bank_name VARCHAR(255) NULL,
   payment_date datetime NULL,
   CONSTRAINT pk_tranx_debit_note_details_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_debit_note_details_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_DETAILS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_debit_note_details_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_DETAILS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_debit_note_details_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_DETAILS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_debit_note_details_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_DETAILS_TBL_ON_SUNDRY_CREDITOR FOREIGN KEY (sundry_creditor_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_debit_note_details_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_DETAILS_TBL_ON_TRANSACTION_STATUS FOREIGN KEY (transaction_status_id) REFERENCES transaction_status_tbl (id);

ALTER TABLE tranx_debit_note_details_tbl ADD CONSTRAINT FK_TRANX_DEBIT_NOTE_DETAILS_TBL_ON_TRANX_DEBITNOTE_MASTER FOREIGN KEY (tranx_debitnote_master_id) REFERENCES tranx_debit_note_new_reference_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;