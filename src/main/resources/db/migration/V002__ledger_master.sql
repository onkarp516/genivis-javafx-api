SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS foundations_tbl;
CREATE TABLE foundations_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  foundation_name VARCHAR(255) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  status BIT(1) NULL,
  CONSTRAINT pk_foundations_tbl PRIMARY KEY (id)
);

INSERT INTO `foundations_tbl` (`id`, `foundation_name`, `created_by`, `created_at`, `status`) VALUES
(1, 'Assets', NULL, '2021-05-15 18:09:52', b'1'),
(2, 'Liabilities', NULL, '2021-05-15 18:10:42', b'1'),
(3, 'Income', NULL, '2021-05-15 18:11:06', b'1'),
(4, 'Expenses', NULL, '2021-05-15 18:11:19', b'1');

DROP TABLE if EXISTS ledger_form_parameter_tbl;
CREATE TABLE ledger_form_parameter_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  form_name VARCHAR(255) NULL,
  slug_name VARCHAR(255) NULL,
  CONSTRAINT pk_ledger_form_parameter_tbl PRIMARY KEY (id)
);

INSERT INTO `ledger_form_parameter_tbl` (`id`, `form_name`, `slug_name`) VALUES
(1, 'Sundry Creditors', 'sundry_creditors'),
(2, 'Sundry Debtors', 'sundry_debtors'),
(3, 'Bank Account', 'bank_account'),
(4, 'Assets', 'assets'),
(5, 'Duties & Taxes', 'duties_taxes'),
(6, 'Others', 'others');

DROP TABLE if EXISTS principles_tbl;
CREATE TABLE principles_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  principle_name VARCHAR(255) NULL,
  unique_code VARCHAR(255) NULL,
  foundation_id BIGINT NULL,
  ledger_form_parameter_id BIGINT NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  status BIT(1) NULL,
  CONSTRAINT pk_principles_tbl PRIMARY KEY (id)
);

ALTER TABLE principles_tbl ADD CONSTRAINT FK_PRINCIPLES_TBL_ON_FOUNDATION FOREIGN KEY (foundation_id) REFERENCES foundations_tbl (id);

ALTER TABLE principles_tbl ADD CONSTRAINT FK_PRINCIPLES_TBL_ON_LEDGER_FORM_PARAMETER FOREIGN KEY (ledger_form_parameter_id) REFERENCES ledger_form_parameter_tbl (id);

INSERT INTO `principles_tbl` (`id`, `principle_name`, `unique_code`, `foundation_id`, `ledger_form_parameter_id`, `created_by`, `created_at`, `status`) VALUES
(1, 'Fixed Assets', 'FIAS', 1, 4, NULL, '2021-05-15 18:19:14', b'1'),
(2, 'Investments', 'INVT', 1, 4, NULL, '2021-05-15 18:20:28',  b'1'),
(3, 'Current Assets', 'CUAS', 1, 4, NULL, '2021-05-15 18:20:42',  b'1'),
(4, 'Capital A/c', 'CPAC', 2, 6, NULL, '2021-05-15 18:21:41', b'1'),
(5, 'Loans (Liabilities)', 'LOAN', 2, 6, NULL, '2021-05-15 18:21:54', b'1'),
(6, 'Current Liabilities', 'CULS', 2, 6, NULL, '2021-05-15 18:22:07', b'1'),
(7, 'Sales Accounts', 'SLAC', 3, 6, NULL, '2021-05-15 18:22:58',  b'1'),
(8, 'Direct Income', 'DIIC', 3, 6, NULL, '2021-05-15 18:23:10', b'1'),
(9, 'Indirect Income', 'INIC', 3, 6, NULL, '2021-05-15 18:23:22',  b'1'),
(10, 'Purchase Accounts', 'PUAC', 4, 6, NULL, '2021-05-15 18:24:03', b'1'),
(11, 'Direct Expenses', 'DIEX', 4, 6, NULL, '2021-05-15 18:24:14',  b'1'),
(12, 'Indirect expenses', 'INEX', 4, 6, NULL, '2021-05-15 18:24:24', b'1');


DROP TABLE if EXISTS principle_groups_tbl;
CREATE TABLE principle_groups_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  group_name VARCHAR(255) NULL,
  unique_code VARCHAR(255) NULL,
  principle_id BIGINT NULL,
  ledger_form_parameter_id BIGINT NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  status BIT(1) NULL,
  CONSTRAINT pk_principle_groups_tbl PRIMARY KEY (id)
);

ALTER TABLE principle_groups_tbl ADD CONSTRAINT FK_PRINCIPLE_GROUPS_TBL_ON_LEDGER_FORM_PARAMETER FOREIGN KEY (ledger_form_parameter_id) REFERENCES ledger_form_parameter_tbl (id);

ALTER TABLE principle_groups_tbl ADD CONSTRAINT FK_PRINCIPLE_GROUPS_TBL_ON_PRINCIPLE FOREIGN KEY (principle_id) REFERENCES principles_tbl (id);

INSERT INTO `principle_groups_tbl` (`id`, `group_name`, `unique_code`, `principle_id`, `ledger_form_parameter_id`, `created_by`, `created_at`, `status`) VALUES
(1, 'Sundry Debtors', 'SUDR', 3, 2, NULL, '2021-05-15 18:28:38', b'1'),
(2, 'Bank Accounts', 'BAAC', 3, 3, NULL, '2021-05-15 18:28:53',  b'1'),
(3, 'Cash-in-hand', 'CAIH', 3, 6, NULL, '2021-05-15 18:29:07',  b'1'),
(4, 'Stock-in-hand', 'STIH', 3, 6, NULL, '2021-05-15 18:29:21',  b'1'),
(5, 'Sundry Creditors', 'SUCR', 6, 1, NULL, '2021-05-15 18:30:09',  b'1'),
(6, 'Duties & Taxes', 'DUTX', 6, 5, NULL, '2021-05-15 18:30:24',  b'1'),
(7, 'Bank O/D', 'BAOD', 6, 6, NULL, '2021-05-15 18:30:34',  b'1');

DROP TABLE if EXISTS associates_groups_tbl;
CREATE TABLE associates_groups_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  associates_name VARCHAR(255) NULL,
  under_id BIGINT NULL,
  status BIT(1) NULL,
  under_prefix VARCHAR(255) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  principle_id BIGINT NULL,
  principle_groups_id BIGINT NULL,
  foundation_id BIGINT NULL,
  branch_id BIGINT NULL,
  outlet_id BIGINT NULL,
  ledger_form_parameter_id BIGINT NULL,
  updated_at datetime NULL,
  updated_by BIGINT NULL,
  CONSTRAINT pk_associates_groups_tbl PRIMARY KEY (id)
);

ALTER TABLE associates_groups_tbl ADD CONSTRAINT FK_ASSOCIATES_GROUPS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE associates_groups_tbl ADD CONSTRAINT FK_ASSOCIATES_GROUPS_TBL_ON_FOUNDATION FOREIGN KEY (foundation_id) REFERENCES foundations_tbl (id);

ALTER TABLE associates_groups_tbl ADD CONSTRAINT FK_ASSOCIATES_GROUPS_TBL_ON_LEDGER_FORM_PARAMETER FOREIGN KEY (ledger_form_parameter_id) REFERENCES ledger_form_parameter_tbl (id);

ALTER TABLE associates_groups_tbl ADD CONSTRAINT FK_ASSOCIATES_GROUPS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE associates_groups_tbl ADD CONSTRAINT FK_ASSOCIATES_GROUPS_TBL_ON_PRINCIPLE FOREIGN KEY (principle_id) REFERENCES principles_tbl (id);

ALTER TABLE associates_groups_tbl ADD CONSTRAINT FK_ASSOCIATES_GROUPS_TBL_ON_PRINCIPLE_GROUPS FOREIGN KEY (principle_groups_id) REFERENCES principle_groups_tbl (id);

DROP TABLE if EXISTS balancing_method_tbl;
CREATE TABLE balancing_method_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  balancing_method VARCHAR(255) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  status BIT(1) NULL,
  CONSTRAINT pk_balancing_method_tbl PRIMARY KEY (id)
);

INSERT INTO `balancing_method_tbl` (`id`, `balancing_method`, `created_by`, `created_at`, `status`) VALUES
(1, 'Bill by Bill', NULL, NULL, b'1'),
(2, 'On Account', NULL, NULL, b'1');

DROP TABLE if EXISTS ledger_master_tbl;
CREATE TABLE ledger_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  ledger_name VARCHAR(255) NULL,
  ledger_code VARCHAR(255) NULL,
  unique_code VARCHAR(255) NULL,
  mailing_name VARCHAR(255) NULL,
  opening_bal_type VARCHAR(255) NULL,
  opening_bal DOUBLE NULL,
  address VARCHAR(255) NULL,
  pincode BIGINT NULL,
  email VARCHAR(255) NULL,
  mobile BIGINT NULL,
  taxable BIT(1) NULL,
  gstin VARCHAR(255) NULL,
  state_code VARCHAR(255) NULL,
  registration_type BIGINT NULL,
  date_of_registration date NULL,
  pancard VARCHAR(255) NULL,
  bank_name VARCHAR(255) NULL,
  account_number VARCHAR(255) NULL,
  ifsc VARCHAR(255) NULL,
  bank_branch VARCHAR(255) NULL,
  tax_type VARCHAR(255) NULL,
  slug_name VARCHAR(255) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  updated_by BIGINT NULL,
  updated_at datetime NULL,
  status BIT(1) NULL,
  under_prefix VARCHAR(255) NULL,
  is_deleted BIT(1) NULL,
  is_default_ledger BIT(1) NULL,
  is_private BIT(1) NULL,
  credit_days INT NULL,
  applicable_from VARCHAR(255) NULL,
  food_license_no VARCHAR(255) NULL,
  tds BIT(1) NULL,
  tds_applicable_date date NULL,
  tcs BIT(1) NULL,
  tcs_applicable_date date NULL,
  district VARCHAR(255) NULL,
  principle_id BIGINT NULL,
  principle_groups_id BIGINT NULL,
  foundation_id BIGINT NULL,
  branch_id BIGINT NULL,
  outlet_id BIGINT NULL,
  country_id BIGINT NULL,
  state_id BIGINT NULL,
  balancing_method_id BIGINT NULL,
  associates_groups_id BIGINT NULL,
  area VARCHAR(255) NULL,
  land_mark VARCHAR(255) NULL,
  city VARCHAR(255) NULL,
  fssai_expiry date NULL,
  sales_rate DOUBLE NULL,
  drug_license_no VARCHAR(255) NULL,
  drug_expiry date NULL,
  license_no VARCHAR(255) NULL,
  license_expiry date NULL,
  food_license_expiry date NULL,
  manufacturing_license_no VARCHAR(255) NULL,
  manufacturing_license_expiry date NULL,
  gst_transfer_date date NULL,
  place VARCHAR(255) NULL,
  business_type VARCHAR(255) NULL,
  business_trade VARCHAR(255) NULL,
  route VARCHAR(255) NULL,
  credit_bill_date date NULL,
  lr_bill_date date NULL,
  anniversary date NULL,
  dob date NULL,
  credit_type_days VARCHAR(255) NULL,
  credit_type_bills VARCHAR(255) NULL,
  credit_type_value VARCHAR(255) NULL,
  credit_num_bills DOUBLE,
  credit_bill_value DOUBLE,
  is_migrated BIT(1) NULL,
  is_first_discount_per_calculate BIT(1) NULL,
  take_discount_amount_in_lumpsum BIT(1) NULL,
  columna VARCHAR(255) NULL,
  columnb VARCHAR(255) NULL,
  columnc VARCHAR(255) NULL,
  columnd VARCHAR(255) NULL,
  columne DOUBLE NULL,
  columnf DOUBLE NULL,
  columng DOUBLE NULL,
  columnh DOUBLE NULL,
  columni date NULL,
  columnj date NULL,
  columnk date NULL,
  columnl date NULL,
  columnm BIGINT NULL,
  columnn BIGINT NULL,
  columno BIT(1) NULL,
  columnp BIT(1) NULL,
  columnq BIT(1) NULL,
  columnr BIT(1) NULL,
  area_id BIGINT NULL,
  salesman_id BIGINT NULL,
  whats_appno BIGINT NULL,
  is_credit BIT(1) NULL,
  is_license BIT(1) NULL,
  is_shipping_details BIT(1) NULL,
  is_department BIT(1) NULL,
  is_bank_details BIT(1) NULL,
  CONSTRAINT pk_ledger_master_tbl PRIMARY KEY (id)
);

ALTER TABLE ledger_master_tbl ADD CONSTRAINT FK_LEDGER_MASTER_TBL_ON_ASSOCIATES_GROUPS FOREIGN KEY (associates_groups_id) REFERENCES associates_groups_tbl (id);

ALTER TABLE ledger_master_tbl ADD CONSTRAINT FK_LEDGER_MASTER_TBL_ON_BALANCING_METHOD FOREIGN KEY (balancing_method_id) REFERENCES balancing_method_tbl (id);

ALTER TABLE ledger_master_tbl ADD CONSTRAINT FK_LEDGER_MASTER_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE ledger_master_tbl ADD CONSTRAINT FK_LEDGER_MASTER_TBL_ON_COUNTRY FOREIGN KEY (country_id) REFERENCES country_tbl (id);

ALTER TABLE ledger_master_tbl ADD CONSTRAINT FK_LEDGER_MASTER_TBL_ON_FOUNDATION FOREIGN KEY (foundation_id) REFERENCES foundations_tbl (id);

ALTER TABLE ledger_master_tbl ADD CONSTRAINT FK_LEDGER_MASTER_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE ledger_master_tbl ADD CONSTRAINT FK_LEDGER_MASTER_TBL_ON_PRINCIPLE FOREIGN KEY (principle_id) REFERENCES principles_tbl (id);

ALTER TABLE ledger_master_tbl ADD CONSTRAINT FK_LEDGER_MASTER_TBL_ON_PRINCIPLE_GROUPS FOREIGN KEY (principle_groups_id) REFERENCES principle_groups_tbl (id);

ALTER TABLE ledger_master_tbl ADD CONSTRAINT FK_LEDGER_MASTER_TBL_ON_STATE FOREIGN KEY (state_id) REFERENCES state_tbl (id);

DROP TABLE if EXISTS ledger_gst_details_tbl;
CREATE TABLE ledger_gst_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  gstin VARCHAR(255) NULL,
  date_of_registration date NULL,
  state_code VARCHAR(255) NULL,
  pan_card VARCHAR(255) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  status BIT(1) NULL,
  ledger_id BIGINT NULL,
  registration_type BIGINT NULL,
  CONSTRAINT pk_ledger_gst_details_tbl PRIMARY KEY (id)
);
ALTER TABLE ledger_gst_details_tbl ADD CONSTRAINT FK_LEDGER_GST_DETAILS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

DROP TABLE if EXISTS ledger_dept_details_tbl;
CREATE TABLE ledger_dept_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  dept VARCHAR(255) NULL,
  name VARCHAR(255) NULL,
  contact_no BIGINT NULL,
  contact_person VARCHAR(255) NULL,
  email VARCHAR(255) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  status BIT(1) NULL,
  ledger_id BIGINT NULL,
  CONSTRAINT pk_ledger_dept_details_tbl PRIMARY KEY (id)
);

ALTER TABLE ledger_dept_details_tbl ADD CONSTRAINT FK_LEDGER_DEPT_DETAILS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

DROP TABLE if EXISTS ledger_shipping_address_tbl;
CREATE TABLE ledger_shipping_address_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  district VARCHAR(255) NULL,
  shipping_address VARCHAR(255) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  status BIT(1) NULL,
  ledger_id BIGINT NULL,
  CONSTRAINT pk_ledger_shipping_address_tbl PRIMARY KEY (id)
);
ALTER TABLE ledger_shipping_address_tbl ADD CONSTRAINT FK_LEDGER_SHIPPING_ADDRESS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

DROP TABLE if EXISTS ledger_billing_address_tbl;
CREATE TABLE ledger_billing_address_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  district VARCHAR(255) NULL,
  billing_address VARCHAR(255) NULL,
  created_by BIGINT NULL,
  created_at datetime NULL,
  status BIT(1) NULL,
  ledger_id BIGINT NULL,
  CONSTRAINT pk_ledger_billing_address_tbl PRIMARY KEY (id)
);
ALTER TABLE ledger_billing_address_tbl ADD CONSTRAINT FK_LEDGER_BILLING_ADDRESS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);
DROP TABLE if EXISTS ledger_bank_details_tbl;
CREATE TABLE ledger_bank_details_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   bank_name VARCHAR(255) NULL,
   account_no VARCHAR(255) NULL,
   ifsc VARCHAR(255) NULL,
   bank_branch VARCHAR(255) NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   status BIT(1) NULL,
   ledger_id BIGINT NULL,
   CONSTRAINT pk_ledger_bank_details_tbl PRIMARY KEY (id)
);

ALTER TABLE ledger_bank_details_tbl ADD CONSTRAINT FK_LEDGER_BANK_DETAILS_TBL_ON_LEDGER FOREIGN KEY (ledger_id) REFERENCES ledger_master_tbl (id);

DROP TABLE if EXISTS ledger_balance_summary_tbl;
CREATE TABLE ledger_balance_summary_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
  foundation_id BIGINT NULL,
  principle_id BIGINT NULL,
  principle_groups_id BIGINT NULL,
  associate_groups_id BIGINT NULL,
  ledger_master_id BIGINT NULL,
  branch_id BIGINT NULL,
  outlet_id BIGINT NULL,
  debit DOUBLE NULL,
  credit DOUBLE NULL,
  opening_bal DOUBLE NULL,
  closing_bal DOUBLE NULL,
  balance DOUBLE NULL,
  under_prefix VARCHAR(255) NULL,
  created_at datetime NULL,
  updated_at datetime NULL,
  status BIT(1) NULL,
  CONSTRAINT pk_ledger_balance_summary_tbl PRIMARY KEY (id)
);

ALTER TABLE ledger_balance_summary_tbl ADD CONSTRAINT FK_LEDGER_BALANCE_SUMMARY_TBL_ON_ASSOCIATE_GROUPS FOREIGN KEY (associate_groups_id) REFERENCES associates_groups_tbl (id);

ALTER TABLE ledger_balance_summary_tbl ADD CONSTRAINT FK_LEDGER_BALANCE_SUMMARY_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE ledger_balance_summary_tbl ADD CONSTRAINT FK_LEDGER_BALANCE_SUMMARY_TBL_ON_FOUNDATION FOREIGN KEY (foundation_id) REFERENCES foundations_tbl (id);

ALTER TABLE ledger_balance_summary_tbl ADD CONSTRAINT FK_LEDGER_BALANCE_SUMMARY_TBL_ON_LEDGER_MASTER FOREIGN KEY (ledger_master_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE ledger_balance_summary_tbl ADD CONSTRAINT FK_LEDGER_BALANCE_SUMMARY_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE ledger_balance_summary_tbl ADD CONSTRAINT FK_LEDGER_BALANCE_SUMMARY_TBL_ON_PRINCIPLE FOREIGN KEY (principle_id) REFERENCES principles_tbl (id);

ALTER TABLE ledger_balance_summary_tbl ADD CONSTRAINT FK_LEDGER_BALANCE_SUMMARY_TBL_ON_PRINCIPLE_GROUPS FOREIGN KEY (principle_groups_id) REFERENCES principle_groups_tbl (id);

DROP TABLE if EXISTS ledger_transaction_postings_tbl;
CREATE TABLE ledger_transaction_postings_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   ledger_master_id BIGINT NULL,
   transaction_type_id BIGINT NULL,
   associate_groups_id BIGINT NULL,
   fiscal_year_id BIGINT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   amount DOUBLE NULL,
   transaction_date datetime NULL,
   transaction_id BIGINT NULL,
   invoice_no VARCHAR(255) NULL,
   ledger_type VARCHAR(255) NULL,
   tranx_type VARCHAR(255) NULL,
   operations VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   updated_by BIGINT NULL,
   updated_at datetime NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_ledger_transaction_postings_tbl PRIMARY KEY (id)
);

ALTER TABLE ledger_transaction_postings_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_POSTINGS_TBL_ON_ASSOCIATE_GROUPS FOREIGN KEY (associate_groups_id) REFERENCES associates_groups_tbl (id);

ALTER TABLE ledger_transaction_postings_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_POSTINGS_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE ledger_transaction_postings_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_POSTINGS_TBL_ON_FISCAL_YEAR FOREIGN KEY (fiscal_year_id) REFERENCES fiscal_year_tbl (id);

ALTER TABLE ledger_transaction_postings_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_POSTINGS_TBL_ON_LEDGER_MASTER FOREIGN KEY (ledger_master_id) REFERENCES ledger_master_tbl (id);

ALTER TABLE ledger_transaction_postings_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_POSTINGS_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE ledger_transaction_postings_tbl ADD CONSTRAINT FK_LEDGER_TRANSACTION_POSTINGS_TBL_ON_TRANSACTION_TYPE FOREIGN KEY (transaction_type_id) REFERENCES transaction_type_master_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;