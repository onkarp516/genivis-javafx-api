SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS courier_services_tbl;
CREATE TABLE courier_services_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   service_name VARCHAR(255) NULL,
   contact_person VARCHAR(255) NULL,
   mobile_number VARCHAR(255) NULL,
   address VARCHAR(255) NULL,
   created_at datetime NULL,
   created_by BIGINT NULL,
   status BIT(1) NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   CONSTRAINT pk_courier_services_tbl PRIMARY KEY (id)
);


ALTER TABLE courier_services_tbl ADD CONSTRAINT FK_COURIER_SERVICES_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE courier_services_tbl ADD CONSTRAINT FK_COURIER_SERVICES_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);