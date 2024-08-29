SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS tranx_gst_output_tbl;
CREATE TABLE tranx_gst_output_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   debtor_id BIGINT NULL,
   posting_ledger_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   payment_mode_id BIGINT NULL,
   roundoff_id BIGINT NULL,
   round_off DOUBLE NULL,
   total_igst DOUBLE NULL,
   total_cgst DOUBLE NULL,
   total_sgst DOUBLE NULL,
   voucher_sr_no VARCHAR(255) NULL,
   voucher_no VARCHAR(255) NULL,
   tranx_date date NULL,
   voucher_date date NULL,
   narrations VARCHAR(255) NULL,
   payment_tranx_no VARCHAR(255) NULL,
   total_amount DOUBLE NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   CONSTRAINT pk_tranx_gst_output_tbl PRIMARY KEY (id)
);
ALTER TABLE tranx_gst_output_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE tranx_gst_output_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TBL_ON_DEBTOR FOREIGN KEY (debtor_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_gst_output_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE tranx_gst_output_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE tranx_gst_output_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TBL_ON_PAYMENT_MODE FOREIGN KEY (payment_mode_id) REFERENCES payment_mode_tbl (id);

ALTER TABLE tranx_gst_output_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TBL_ON_POSTING_LEDGER FOREIGN KEY (posting_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_gst_output_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TBL_ON_ROUNDOFF FOREIGN KEY (roundoff_id) REFERENCES ledger_master_tbl (id);

DROP TABLE if EXISTS tranx_gst_ouput_details_tbl;
CREATE TABLE tranx_gst_ouput_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   gst_ouput_id BIGINT NOT NULL,
   product_id BIGINT NULL,
   hsn_id BIGINT NULL,
   tax_id BIGINT NULL,
   particular VARCHAR(255) NULL,
   hsn_no VARCHAR(255) NULL,
   igst DOUBLE NULL,
   cgst DOUBLE NULL,
   sgst DOUBLE NULL,
   amount DOUBLE NULL,
   qty DOUBLE NULL,
   final_amt DOUBLE NULL,
   status BIT(1) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   base_amount DOUBLE NULL,
   CONSTRAINT pk_tranx_gst_ouput_details_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_gst_ouput_details_tbl ADD CONSTRAINT FK_TRANX_GST_OUPUT_DETAILS_TBL_ON_GST_OUPUT FOREIGN KEY (gst_ouput_id) REFERENCES tranx_gst_output_tbl (id);

ALTER TABLE tranx_gst_ouput_details_tbl ADD CONSTRAINT FK_TRANX_GST_OUPUT_DETAILS_TBL_ON_HSN FOREIGN KEY (hsn_id) REFERENCES product_hsn_tbl (id);

ALTER TABLE tranx_gst_ouput_details_tbl ADD CONSTRAINT FK_TRANX_GST_OUPUT_DETAILS_TBL_ON_PRODUCT FOREIGN KEY (product_id) REFERENCES product_tbl (id);

ALTER TABLE tranx_gst_ouput_details_tbl ADD CONSTRAINT FK_TRANX_GST_OUPUT_DETAILS_TBL_ON_TAX FOREIGN KEY (tax_id) REFERENCES tax_master_tbl (id);
DROP TABLE if EXISTS tranx_gst_output_tax_tbl;
CREATE TABLE tranx_gst_output_tax_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   gst_output_id BIGINT NOT NULL,
   debtors_id BIGINT NOT NULL,
   duties_taxes_ledger_id BIGINT NOT NULL,
   posting_ledger_id BIGINT NULL,
   amount DOUBLE NULL,
   status BIT(1) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   CONSTRAINT pk_tranx_gst_output_tax_tbl PRIMARY KEY (id)
);

ALTER TABLE tranx_gst_output_tax_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TAX_TBL_ON_DEBTORS FOREIGN KEY (debtors_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_gst_output_tax_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TAX_TBL_ON_DUTIES_TAXES_LEDGER FOREIGN KEY (duties_taxes_ledger_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE tranx_gst_output_tax_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TAX_TBL_ON_GST_OUTPUT FOREIGN KEY (gst_output_id) REFERENCES tranx_gst_output_tbl (id);

ALTER TABLE tranx_gst_output_tax_tbl ADD CONSTRAINT FK_TRANX_GST_OUTPUT_TAX_TBL_ON_POSTING_LEDGER FOREIGN KEY (posting_ledger_id) REFERENCES ledger_master_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;
