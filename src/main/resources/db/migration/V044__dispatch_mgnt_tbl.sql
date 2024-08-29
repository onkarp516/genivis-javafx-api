SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS delivery_boy_tbl;
CREATE TABLE delivery_boy_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   first_name VARCHAR(255) NULL,
   last_name VARCHAR(255) NULL,
   mobile_no BIGINT NULL,
   address VARCHAR(255) NULL,
   identity_document VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_delivery_boy_tbl PRIMARY KEY (id)
);

DROP TABLE if EXISTS transport_agency_tbl;
CREATE TABLE transport_agency_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   transport_agency_name VARCHAR(255) NULL,
   address VARCHAR(255) NULL,
   contact_no BIGINT NULL,
   contact_person VARCHAR(255) NULL,
   pincode VARCHAR(255) NULL,
   country_id BIGINT NULL,
   state_id BIGINT NULL,
   city_id BIGINT NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_transport_agency_tbl PRIMARY KEY (id)
);

DROP TABLE if EXISTS city_tbl;
CREATE TABLE city_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   name VARCHAR(50) NOT NULL,
   state_id MEDIUMINT NULL,
   state_code VARCHAR(255) NULL,
   country_id MEDIUMINT NULL,
   country_code CHAR NULL,
   CONSTRAINT pk_city_tbl PRIMARY KEY (id)
);


SET FOREIGN_KEY_CHECKS = 1;

