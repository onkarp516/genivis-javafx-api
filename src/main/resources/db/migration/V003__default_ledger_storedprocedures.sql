SET FOREIGN_KEY_CHECKS = 0;
DROP PROCEDURE if EXISTS default_ledgers;
DELIMITER &&
CREATE PROCEDURE default_ledgers(IN branch_id BIGINT,IN outlet_id BIGINT,IN created_by BIGINT)
BEGIN
    INSERT INTO `ledger_master_tbl` (`ledger_name`, `foundation_id`, `principle_groups_id`, `principle_id`, `branch_id`,
     `outlet_id`, `account_number`, `address`, `bank_branch`, `bank_name`, `created_by`, `created_at`,
      `date_of_registration`, `email`, `gstin`, `ifsc`, `mailing_name`, `mobile`, `opening_bal`,  `opening_bal_type`,
      `pancard`, `pincode`, `registration_type`, `state_code`, `status`, `tax_type`, `taxable`,  `updated_by`, `updated_at`,
       `country_id`, `state_id`, `slug_name`, `unique_code`,`under_prefix`, `is_deleted`,`is_default_ledger`) VALUES
    ('Cash A/c',1,3,3,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','NA',b'0',0,now(),NULL,NULL,'others','CAIH','PG#3',b'0',b'1'),
    ('Round Off',4,NULL,12,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','NA',b'0',0,now(),NULL,NULL,'others','INEX','P#12',b'0',b'1'),
    ('Purchase Discount',3,NULL,9,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','NA',b'0',0,now(),NULL,NULL,'others','INIC','P#9',b'0',b'1'),
    ('Sales Discount',4,NULL,12,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','NA',b'0',0,now(),NULL,NULL,'others','INEX','P#12',b'0',b'1');
END &&
DELIMITER ;
DROP PROCEDURE if EXISTS duties_taxes_registered_outlet;
DELIMITER &&
CREATE PROCEDURE duties_taxes_registered_outlet(IN branch_id BIGINT,IN outlet_id BIGINT, IN created_by BIGINT)
BEGIN
INSERT INTO `ledger_master_tbl` (`ledger_name`, `foundation_id`, `principle_groups_id`, `principle_id`, `branch_id`,
`outlet_id`, `account_number`, `address`, `bank_branch`, `bank_name`, `created_by`, `created_at`,
 `date_of_registration`, `email`, `gstin`, `ifsc`,`mailing_name`, `mobile`, `opening_bal`,`opening_bal_type`, `pancard`,
 `pincode`, `registration_type`, `state_code`, `status`, `tax_type`, `taxable`,`updated_by`, `updated_at`, `country_id`,
  `state_id`, `slug_name`, `unique_code`,`under_prefix`, `is_deleted`,`is_default_ledger`) VALUES
('INPUT CGST 1.5',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT SGST 1.5',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT IGST 3',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT CGST 2.5',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT SGST 2.5',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT IGST 5',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT CGST 6',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT SGST 6',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT IGST 12',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT CGST 9',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT SGST 9',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT IGST 18',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT CGST 14',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT SGST 14',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('INPUT IGST 28',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT CGST 1.5',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT SGST 1.5',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT IGST 3',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT CGST 2.5',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT SGST 2.5',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT IGST 5',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT CGST 6',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT SGST 6',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT IGST 12',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT CGST 9',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT SGST 9',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT IGST 18',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT CGST 14',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT SGST 14',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),

('OUTPUT IGST 28',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'CR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1');

END &&
DELIMITER ;
DROP PROCEDURE if EXISTS duties_taxes_unregistered_outlet;
DELIMITER &&
CREATE PROCEDURE duties_taxes_unregistered_outlet(IN branch_id BIGINT,IN outlet_id BIGINT, IN created_by BIGINT)
BEGIN
INSERT INTO `ledger_master_tbl` (`ledger_name`, `foundation_id`, `principle_groups_id`, `principle_id`, `branch_id`, `outlet_id`, `account_number`, `address`, `bank_branch`, `bank_name`, `created_by`, `created_at`,`date_of_registration`, `email`, `gstin`, `ifsc`,`mailing_name`, `mobile`, `opening_bal`,`opening_bal_type`, `pancard`, `pincode`, `registration_type`, `state_code`, `status`, `tax_type`, `taxable`,`updated_by`, `updated_at`, `country_id`, `state_id`, `slug_name`, `unique_code`,`under_prefix`, `is_deleted`,`is_default_ledger`) VALUES
('INPUT CGST 1.5',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT SGST 1.5',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT IGST 3',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT CGST 2.5',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT SGST 2.5',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT IGST 5',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT CGST 6',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT SGST 6',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT IGST 12',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT CGST 9',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT SGST 9',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT IGST 18',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT CGST 14',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT SGST 14',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),

('INPUT IGST 28',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','integrated_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1');

END &&
DELIMITER ;

DROP TRIGGER if EXISTS after_ledger_insert;

DELIMITER $$

--CREATE TRIGGER after_ledger_insert
--AFTER INSERT
--ON ledger_master_tbl FOR EACH ROW
--BEGIN
--    DECLARE CHECKEXIST INT;
--    SELECT EXISTS (SELECT * from ledger_balance_summary_tbl where ledger_balance_summary_tbl.ledger_master_id=NEW.id) INTO CHECKEXIST;
--
--    IF CHECKEXIST=0 THEN
--      INSERT INTO `ledger_balance_summary_tbl` (`ledger_master_id`, `opening_bal`, `debit`, `credit`, `closing_bal`, `under_prefix`, `foundation_id`, `principle_id`, `principle_groups_id`, `balance`, `created_at`, `updated_at`, `status`, `branch_id`, `outlet_id`, `fiscal_year_id`) VALUES (NEW.id,NEW.opening_bal,0,0,0,NEW.under_prefix,NEW.foundation_id, NEW.principle_id, NEW.principle_groups_id, 0,NOW(),NOW(),NEW.status,NEW.branch_id,NEW.outlet_id, fiscalYearId);
--    END IF;
--END $$
--DELIMITER ;



