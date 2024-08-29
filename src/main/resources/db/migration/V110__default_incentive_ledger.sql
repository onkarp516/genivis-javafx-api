SET FOREIGN_KEY_CHECKS = 0;
DROP PROCEDURE if EXISTS default_incentive_ledger;
DELIMITER &&
CREATE PROCEDURE default_incentive_ledger(IN ledger_name VARCHAR(255),IN ledger_code VARCHAR(255),IN outlet_id
BIGINT, IN created_by BIGINT)
BEGIN
INSERT INTO `ledger_master_tbl` (`ledger_name`, `ledger_code`, `unique_code`, `mailing_name`, `opening_bal_type`,
`opening_bal`, `address`,`taxable`, `gstin`, `state_code`, `registration_type`, `date_of_registration`, `pancard`,
`bank_name`,`account_number`, `ifsc`, `bank_branch`, `tax_type`, `slug_name`, `created_by`, `created_at`,
`updated_by`, `updated_at`, `status`, `under_prefix`, `is_deleted`, `is_default_ledger`, `is_private`, `principle_id`,
`foundation_id`,`branch_id`, `outlet_id`, `balancing_method_id`, `associates_groups_id`) VALUES
(ledger_name,ledger_code, 'INEX','', 'Dr', 0,'', b'0', 'NA','', 3, NULL, NULL, 'NA', 'NA', 'NA', 'NA', 'NA', 'others',
created_by, NULL, NULL, NULL, b'1', 'P#12', b'0', b'1', b'0', 12, 4, NULL, outlet_id,  2, NULL);
END &&
DELIMITER;
SET FOREIGN_KEY_CHECKS = 1;
