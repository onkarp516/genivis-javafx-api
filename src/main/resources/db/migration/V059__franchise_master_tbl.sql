SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS franchise_master_tbl;
CREATE TABLE franchise_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   franchise_name VARCHAR(255) NULL,
   franchise_code VARCHAR(255) NULL,
   applicant_name VARCHAR(255) NULL,
   partner_name VARCHAR(255) NULL,
   district_id BIGINT NULL,
   regional_id BIGINT NULL,
   zone_id BIGINT NULL,
   state_id BIGINT NULL,
   invest_amt BIGINT NULL,
   franchise_address VARCHAR(255) NULL,
   residencial_address VARCHAR(255) NULL,
   pincode VARCHAR(255) NULL,
   corp_pincode VARCHAR(255) NULL,
   mobile_number BIGINT NULL,
   whatsapp_number BIGINT NULL,
   gender VARCHAR(255) NULL,
   dob date NULL,
   age BIGINT NULL,
   education BIGINT NULL,
   present_occupation VARCHAR(255) NULL,
   aadar_upload VARCHAR(255) NULL,
   pan_upload VARCHAR(255) NULL,
   dl1upload VARCHAR(255) NULL,
   dl2upload VARCHAR(255) NULL,
   dl3upload VARCHAR(255) NULL,
   bank_upload VARCHAR(255) NULL,
   bank_name VARCHAR(255) NULL,
   account_number VARCHAR(255) NULL,
   ifsc VARCHAR(255) NULL,
   bank_branch VARCHAR(255) NULL,
   email VARCHAR(255) NULL,
   state_code VARCHAR(255) NULL,
   currency VARCHAR(255) NULL,
   company_data_path VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   country_id BIGINT NULL,
   business_type VARCHAR(255) NULL,
   business_trade VARCHAR(255) NULL,
   is_same_address BIT(1) NULL,
   area VARCHAR(255) NULL,
   district VARCHAR(255) NULL,
   residencial_area VARCHAR(255) NULL,
   residencial_state VARCHAR(255) NULL,
   residencial_district VARCHAR(255) NULL,
   CONSTRAINT pk_franchise_master_tbl PRIMARY KEY (id)
);

CREATE TABLE franchise_master_tbl_branches (
  franchise_master_id BIGINT NOT NULL,
   branches_id BIGINT NOT NULL
);

ALTER TABLE franchise_master_tbl_branches ADD CONSTRAINT uc_franchise_master_tbl_branches_branches UNIQUE (branches_id);

ALTER TABLE franchise_master_tbl ADD CONSTRAINT FK_FRANCHISE_MASTER_TBL_ON_COUNTRY FOREIGN KEY (country_id) REFERENCES country_tbl (id);

ALTER TABLE franchise_master_tbl ADD CONSTRAINT FK_FRANCHISE_MASTER_TBL_ON_STATE FOREIGN KEY (state_id) REFERENCES state_tbl (id);

ALTER TABLE franchise_master_tbl_branches ADD CONSTRAINT fk_framastblbra_on_branch FOREIGN KEY (branches_id) REFERENCES branch_tbl (id);

ALTER TABLE franchise_master_tbl_branches ADD CONSTRAINT fk_framastblbra_on_franchise_master FOREIGN KEY (franchise_master_id) REFERENCES franchise_master_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;