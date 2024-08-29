SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS patient_master_tbl;
CREATE TABLE patient_master_tbl (
   id BIGINT AUTO_INCREMENT NOT NULL,
     patient_name VARCHAR(255) NULL,
     patient_address VARCHAR(255) NULL,
     mobile_number VARCHAR(255) NULL,
     age BIGINT NULL,
     weight BIGINT NULL,
     birth_date date NULL,
     id_no VARCHAR(255) NULL,
     gender VARCHAR(255) NULL,
     pincode BIGINT NULL,
     tb_diagnosis_date date NULL,
     tb_treatment_initiation_date date NULL,
     blood_group VARCHAR(255) NULL,
     branch_id BIGINT NULL,
     outlet_id BIGINT NULL,
        created_at datetime NULL,
        created_by BIGINT NULL,
        status BIT(1) NULL,
        updated_at datetime NULL,
        updated_by BIGINT NULL,
     CONSTRAINT pk_patient_master_tbl PRIMARY KEY (id)
);
SET FOREIGN_KEY_CHECKS = 1;