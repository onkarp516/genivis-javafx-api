SET FOREIGN_KEY_CHECKS = 0;
DROP PROCEDURE if EXISTS provision_ledger_group;
DELIMITER &&
CREATE PROCEDURE provision_ledger_group(IN outlet_id BIGINT, IN created_by BIGINT)
BEGIN
INSERT INTO `associates_groups_tbl` (`associates_name`,`under_id`,`status`,`under_prefix`,`created_by`,`principle_id`,
`foundation_id`,`outlet_id`,`ledger_form_parameter_id`) VALUES
('Provisions',6,b'1','P#6',created_by,6,2,outlet_id, 6);
END &&
DELIMITER;
SET FOREIGN_KEY_CHECKS = 1;
