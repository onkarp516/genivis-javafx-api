SET FOREIGN_KEY_CHECKS = 0;
DROP PROCEDURE if EXISTS create_counter_customer_ledger;
DELIMITER &&
CREATE PROCEDURE create_counter_customer_ledger(IN outlet_id BIGINT,IN created_by BIGINT)
BEGIN
   INSERT INTO `ledger_master_tbl` (`ledger_name`, `ledger_code`, `unique_code`, `mailing_name`, `opening_bal_type`, `opening_bal`, `address`,
   `pincode`, `email`, `mobile`, `taxable`, `gstin`, `state_code`, `registration_type`, `date_of_registration`, `pancard`, `bank_name`,
    `account_number`, `ifsc`, `bank_branch`, `tax_type`, `slug_name`, `created_by`, `created_at`, `updated_by`, `updated_at`, `status`,
    `under_prefix`, `is_deleted`, `is_default_ledger`, `is_private`, `credit_days`, `applicable_from`, `food_license_no`, `tds`,
     `tds_applicable_date`, `tcs`, `tcs_applicable_date`, `district`, `principle_id`, `principle_groups_id`, `foundation_id`,
     `branch_id`, `outlet_id`, `country_id`, `state_id`,
    `balancing_method_id`, `associates_groups_id`) VALUES
     ('Counter Customer', 'CNCS', 'SUDR', 'Counter_Customer', 'Dr', 0, 'Solapur', 413005, 'c@gmail.com', 9879879879, b'0', 'NA',
     '27', 3, NULL, NULL, 'NA', 'NA', 'NA', 'NA', 'NA', 'sundry_debtors', created_by, '2022-10-10 16:54:11',
     NULL, '2022-10-10 16:54:11', b'1', 'PG#1', b'0', b'1', b'0', 0, 'NA', 'NA', NULL, NULL, NULL, NULL, NULL, 3, 1, 1, NULL, outlet_id, 101, 4008, 2, NULL);
END &&
DELIMITER ;
SET FOREIGN_KEY_CHECKS = 1;