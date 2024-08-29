SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS transaction_type_master_tbl;
CREATE TABLE transaction_type_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  transaction_name VARCHAR(255) NULL,
  transaction_code VARCHAR(255) NULL,
  CONSTRAINT pk_transaction_type_master_tbl PRIMARY KEY (id)
);

INSERT INTO `transaction_type_master_tbl` (`id`, `transaction_code`, `transaction_name`) VALUES
(1, 'PRS', 'purchase invoice'),
(2, 'PRSRT', 'purchase return'),
(3, 'SLS', 'sales invoice'),
(4, 'SLSRT', 'sales return'),
(5, 'RCPT', 'receipt'),
(6, 'PMT', 'payment'),
(7, 'DBTN', 'debit note'),
(8, 'CRDTN', 'credit note'),
(9, 'CNTR', 'contra'),
(10, 'JRNL', 'journal'),
(11, 'PRSORD', 'purchase order'),
(12, 'PRSCHN', 'purchase challan'),
(13, 'SLSQTN', 'sales quotation'),
(14, 'SLSORD', 'sales order'),
(15, 'SLSCHN', 'sales challan'),
(16, 'CNTS', 'counter sales');


DROP TABLE if EXISTS transaction_status_tbl;
CREATE TABLE transaction_status_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  status_name VARCHAR(255) NULL,
  status BIT(1) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  updated_at datetime NULL,
  updated_by BIGINT NULL,
  CONSTRAINT pk_transaction_status_tbl PRIMARY KEY (id)
);
INSERT INTO `transaction_status_tbl` (`id`, `status_name`, `status`, `created_by`, `created_at`, `updated_at`, `updated_by`) VALUES
(1, 'opened', b'1', NULL, '2021-10-01 12:57:28', '2021-10-01 12:57:28', NULL),
(2, 'closed', b'1', NULL, '2021-10-01 12:58:27', '2021-10-01 12:58:27', NULL);


DROP TABLE if EXISTS ledger_transaction_details_tbl;
CREATE TABLE ledger_transaction_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  foundation_id BIGINT NULL,
  principle_id BIGINT NULL,
  principle_groups_id BIGINT NULL,
  ledger_master_id BIGINT NULL,
  transaction_type_id BIGINT NULL,
  balancing_method_id BIGINT NULL,
  associate_groups_id BIGINT NULL,
  branch_id BIGINT NULL,
  outlet_id BIGINT NULL,
  payment_status VARCHAR(255) NULL,
  debit DOUBLE NULL,
  credit DOUBLE NULL,
  opening_bal DOUBLE NULL,
  closing_bal DOUBLE NULL,
  transaction_date date NULL,
  payment_date date NULL,
  transaction_id BIGINT NULL,
  tranx_type VARCHAR(255) NULL,
  financial_year VARCHAR(255) NULL,
  under_prefix VARCHAR(255) NULL,
  created_at datetime NULL,
  created_by BIGINT NULL,
  updated_by BIGINT NULL,
  updated_at datetime NULL,
  status BIT(1) NULL,
  invoice_no VARCHAR(255) NULL,
  CONSTRAINT pk_ledger_transaction_details_tbl PRIMARY KEY (id)
);

ALTER TABLE ledger_transaction_details_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_DETAILS_TBL_ON_ASSOCIATE_GROUPS FOREIGN KEY (associate_groups_id) REFERENCES associates_groups_tbl (id);

ALTER TABLE ledger_transaction_details_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_DETAILS_TBL_ON_BALANCING_METHOD FOREIGN KEY (balancing_method_id) REFERENCES balancing_method_tbl (id);

ALTER TABLE ledger_transaction_details_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_DETAILS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE ledger_transaction_details_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_DETAILS_TBL_ON_FOUNDATION FOREIGN KEY (foundation_id) REFERENCES foundations_tbl (id);

ALTER TABLE ledger_transaction_details_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_DETAILS_TBL_ON_LEDGER_MASTER FOREIGN KEY (ledger_master_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE ledger_transaction_details_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_DETAILS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE ledger_transaction_details_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_DETAILS_TBL_ON_PRINCIPLE FOREIGN KEY (principle_id) REFERENCES principles_tbl (id);

ALTER TABLE ledger_transaction_details_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_DETAILS_TBL_ON_PRINCIPLE_GROUPS FOREIGN KEY (principle_groups_id) REFERENCES principle_groups_tbl (id);

ALTER TABLE ledger_transaction_details_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_DETAILS_TBL_ON_TRANSACTION_TYPE FOREIGN KEY (transaction_type_id) REFERENCES transaction_type_master_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;