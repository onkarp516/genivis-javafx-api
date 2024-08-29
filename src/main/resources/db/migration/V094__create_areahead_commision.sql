SET FOREIGN_KEY_CHECKS = 0;
CREATE TABLE areahead_commission_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   areahead_id BIGINT NULL,
   areahead_role VARCHAR(255) NULL,
   sales_invoice_number VARCHAR(255) NULL,
   sales_invoice_amount DOUBLE NULL,
   commission_percentage DOUBLE NULL,
   commission_amount DOUBLE NULL,
   invoice_base_amount DOUBLE NULL,
   invoice_date date NULL,
   franchise_code VARCHAR(255) NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   created_by BIGINT NULL,
   updated_by BIGINT NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_areahead_commission_tbl PRIMARY KEY (id)
);
SET FOREIGN_KEY_CHECKS = 1;