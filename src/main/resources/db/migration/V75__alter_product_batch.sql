ALTER TABLE product_batchno_tbl
ADD COLUMN  dis_per DOUBLE NULL,
ADD COLUMN  dis_amt DOUBLE NULL,
ADD COLUMN  cess_per DOUBLE NULL,
ADD COLUMN  cess_amt DOUBLE NULL,
ADD COLUMN  barcode DOUBLE NULL,
ADD COLUMN  pur_date date NULL,
ADD COLUMN  modify_date date NULL;