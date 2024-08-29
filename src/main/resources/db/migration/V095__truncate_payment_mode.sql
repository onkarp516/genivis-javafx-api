SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE payment_mode_tbl;
SET FOREIGN_KEY_CHECKS = 1;

 ALTER TABLE  tranx_purchase_invoice_tbl
 ADD COLUMN payment_mode VARCHAR(255) NULL;