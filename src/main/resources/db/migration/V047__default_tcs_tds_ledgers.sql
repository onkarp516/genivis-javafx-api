DROP PROCEDURE if EXISTS tcstds_registered_outlet;
DELIMITER &&
CREATE PROCEDURE tcstds_registered_outlet(IN branch_id BIGINT,IN outlet_id BIGINT, IN created_by BIGINT)
BEGIN
INSERT INTO `ledger_master_tbl` (`ledger_name`,`ledger_code`,`foundation_id`, `principle_groups_id`, `principle_id`, `branch_id`,
`outlet_id`, `account_number`, `address`, `bank_branch`, `bank_name`, `created_by`, `created_at`,
 `date_of_registration`, `email`, `gstin`, `ifsc`,`mailing_name`, `mobile`, `opening_bal`,`opening_bal_type`, `pancard`,
 `pincode`, `registration_type`, `state_code`, `status`, `tax_type`, `taxable`,`updated_by`, `updated_at`, `country_id`,
  `state_id`, `slug_name`, `unique_code`,`under_prefix`, `is_deleted`,`is_default_ledger`) VALUES
('TCS','tcs',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1'),
('TDS','tds',2,6,6,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'duties_taxes','DUTX','PG#6',b'0',b'1');
END &&
DELIMITER;
DROP PROCEDURE if EXISTS tcstds_unregistered_outlet;
DELIMITER &&
CREATE PROCEDURE tcstds_unregistered_outlet(IN branch_id BIGINT,IN outlet_id BIGINT, IN created_by BIGINT)
BEGIN
INSERT INTO `ledger_master_tbl` (`ledger_name`,`ledger_code`,`foundation_id`, `principle_groups_id`, `principle_id`, `branch_id`, `outlet_id`, `account_number`, `address`, `bank_branch`, `bank_name`, `created_by`, `created_at`,`date_of_registration`, `email`, `gstin`, `ifsc`,`mailing_name`, `mobile`, `opening_bal`,`opening_bal_type`, `pancard`, `pincode`, `registration_type`, `state_code`, `status`, `tax_type`, `taxable`,`updated_by`, `updated_at`, `country_id`, `state_id`, `slug_name`, `unique_code`,`under_prefix`, `is_deleted`,`is_default_ledger`) VALUES
('TCS','tcs',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','central_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1'),
('TDS','tds',4,NULL,11,branch_id,outlet_id,'NA','NA','NA','NA',created_by,now(),NULL,'NA','NA','NA','NA',0,0,'DR','NA',0,NULL,'NA',b'1','state_tax',b'0',0,now(),NULL,NULL,'others','DIEX','P#11',b'0',b'1');

END &&
DELIMITER;

DROP PROCEDURE if EXISTS default_pur_sales_ac;
DELIMITER &&
CREATE PROCEDURE default_pur_sales_ac(IN branch_id BIGINT,IN outlet_id BIGINT, IN created_by BIGINT)
BEGIN
INSERT INTO `ledger_master_tbl` (`ledger_name`, `outlet_id`, `branch_id`, `ledger_code`, `unique_code`, `opening_bal_type`, `opening_bal`, `slug_name`, `created_by`, `created_at`, `status`, `under_prefix`, `is_deleted`, `is_default_ledger`, `is_private`, `principle_id`, `principle_groups_id`, `foundation_id`, `balancing_method_id`, `associates_groups_id`) VALUES
('Purchase A/C', outlet_id, branch_id, 'pac', 'PUAC',  'Dr', 0, 'others', created_by, now(),  b'1', 'P#10', b'0',  b'1',  b'0',10, NULL, 4, 2, NULL),
('Sales A/C', outlet_id, branch_id, 'sac', 'SLAC', 'Dr', 0, 'others', created_by, now(),  b'1', 'P#7', b'0',  b'1',  b'0', 7,NULL, 3, 2, NULL);
END &&
DELIMITER;

DROP PROCEDURE if EXISTS default_tax_masters;
DELIMITER &&
CREATE PROCEDURE default_tax_masters(IN branch_id BIGINT,IN outlet_id BIGINT, IN created_by BIGINT)
BEGIN
INSERT INTO `tax_master_tbl` ( `gst_per`, `cgst`, `sgst`, `igst`, `sratio`, `applicable_date`, `created_by`, `created_at`, `status`, `branch_id`, `outlet_id`) VALUES
('0', 0, 0, 0, 50, NULL, created_by, now(), b'1', branch_id, outlet_id),
('3', 1.5, 1.5, 3, 50, NULL, created_by, now(), b'1', branch_id, outlet_id),
('5', 2.5, 2.5, 5, 50, NULL, created_by,now(), b'1', branch_id, outlet_id),
('12', 6, 6, 12, 50, NULL, created_by, now(), b'1', branch_id, outlet_id),
('18', 9, 9, 18, 50, NULL, created_by,now(), b'1', branch_id, outlet_id),
('28', 14, 14, 28, 50, NULL, created_by,now(), b'1', branch_id, outlet_id);
END &&
DELIMITER;
