ALTER TABLE product_tbl
 ADD COLUMN is_mis BIT(1) NULL,
 ADD COLUMN is_prescription BIT(1) NULL,
 ADD COLUMN is_formulation BIT(1) NULL,
 ADD COLUMN upload_image VARCHAR(255) NULL,
 ADD COLUMN drug_contents VARCHAR(255) NULL;

ALTER TABLE product_content_master_tbl
 ADD COLUMN content_type_dose VARCHAR(255) NULL;