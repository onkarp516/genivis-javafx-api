SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS area_head_tbl;
CREATE TABLE area_head_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   first_name VARCHAR(255) NULL,
   middle_name VARCHAR(255) NULL,
   last_name VARCHAR(255) NULL,
   email VARCHAR(255) NULL,
   mobile_number VARCHAR(255) NULL,
   whatsapp_number VARCHAR(255) NULL,
   birth_date date NULL,
   address VARCHAR(255) NULL,
   gender VARCHAR(255) NULL,
   permenant_address VARCHAR(255) NULL,
   is_same_address BIT(1) NULL,
   pincode VARCHAR(255) NULL,
   corp_pincode VARCHAR(255) NULL,
   city VARCHAR(255) NULL,
   corporate_city VARCHAR(255) NULL,
   area VARCHAR(255) NULL,
   corporate_area VARCHAR(255) NULL,
   temporary_address VARCHAR(255) NULL,
   aadhar_card_no VARCHAR(255) NULL,
   aadhar_card_file VARCHAR(255) NULL,
   pan_card_no VARCHAR(255) NULL,
   pan_card_file VARCHAR(255) NULL,
   bank_acc_name VARCHAR(255) NULL,
   bank_acc_no VARCHAR(255) NULL,
   bank_accifsc VARCHAR(255) NULL,
   bank_acc_file VARCHAR(255) NULL,
   area_role VARCHAR(255) NULL,
   state_code VARCHAR(255) NULL,
   zone_code VARCHAR(255) NULL,
   region_code VARCHAR(255) NULL,
   district_code VARCHAR(255) NULL,
   partner_deed_file VARCHAR(255) NULL,
   username VARCHAR(255) NULL,
   password VARCHAR(255) NULL,
   plain_password VARCHAR(255) NULL,
   status BIT(1) NULL,
   country_id BIGINT NULL,
   state_id BIGINT NULL,
   zone_id BIGINT NULL,
   region_id BIGINT NULL,
   district_id BIGINT NULL,
   CONSTRAINT pk_area_head_tbl PRIMARY KEY (id)
);

ALTER TABLE area_head_tbl ADD CONSTRAINT FK_AREA_HEAD_TBL_ON_COUNTRY FOREIGN KEY (country_id) REFERENCES country_tbl (id);

ALTER TABLE area_head_tbl ADD CONSTRAINT FK_AREA_HEAD_TBL_ON_DISTRICT FOREIGN KEY (district_id) REFERENCES district_tbl (id);

ALTER TABLE area_head_tbl ADD CONSTRAINT FK_AREA_HEAD_TBL_ON_REGION FOREIGN KEY (region_id) REFERENCES region_tbl (id);

ALTER TABLE area_head_tbl ADD CONSTRAINT FK_AREA_HEAD_TBL_ON_STATE FOREIGN KEY (state_id) REFERENCES state_tbl (id);

ALTER TABLE area_head_tbl ADD CONSTRAINT FK_AREA_HEAD_TBL_ON_ZONE FOREIGN KEY (zone_id) REFERENCES zone_tbl (id);


DROP TABLE if EXISTS zone_tbl;
CREATE TABLE zone_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   state_id BIGINT NULL,
   zone_name VARCHAR(255) NULL,
    status BIT(1) NULL,
   CONSTRAINT pk_zone_tbl PRIMARY KEY (id)
);

ALTER TABLE zone_tbl ADD CONSTRAINT FK_ZONE_TBL_ON_STATE FOREIGN KEY (state_id) REFERENCES state_tbl (id);

DROP TABLE if EXISTS region_tbl;
CREATE TABLE region_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   state_id BIGINT NULL,
   zone_id BIGINT NULL,
   region_name VARCHAR(255) NULL,
    status BIT(1) NULL,
   CONSTRAINT pk_region_tbl PRIMARY KEY (id)
);



ALTER TABLE region_tbl ADD CONSTRAINT FK_REGION_TBL_ON_STATE FOREIGN KEY (state_id) REFERENCES state_tbl (id);

ALTER TABLE region_tbl ADD CONSTRAINT FK_REGION_TBL_ON_ZONE FOREIGN KEY (zone_id) REFERENCES zone_tbl (id);

DROP TABLE if EXISTS district_tbl;
CREATE TABLE district_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   district_name VARCHAR(255) NULL,
    status BIT(1) NULL,
   state_id BIGINT NULL,
   zone_id BIGINT NULL,
   region_id BIGINT NULL,
   CONSTRAINT pk_district_tbl PRIMARY KEY (id)
);

ALTER TABLE district_tbl ADD CONSTRAINT FK_DISTRICT_TBL_ON_REGION FOREIGN KEY (region_id) REFERENCES region_tbl (id);

ALTER TABLE district_tbl ADD CONSTRAINT FK_DISTRICT_TBL_ON_STATE FOREIGN KEY (state_id) REFERENCES state_tbl (id);

ALTER TABLE district_tbl ADD CONSTRAINT FK_DISTRICT_TBL_ON_ZONE FOREIGN KEY (zone_id) REFERENCES zone_tbl (id);



SET FOREIGN_KEY_CHECKS = 1;