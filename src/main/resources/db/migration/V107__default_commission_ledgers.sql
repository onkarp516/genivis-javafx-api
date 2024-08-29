SET FOREIGN_KEY_CHECKS = 0;
DROP PROCEDURE if EXISTS commission_ledger_group;
DELIMITER &&
CREATE PROCEDURE commission_ledger_group(IN outlet_id BIGINT, IN created_by BIGINT)
BEGIN
INSERT INTO `associates_groups_tbl` (`associates_name`,`under_id`,`status`,`under_prefix`,`created_by`,`principle_id`,
`principle_groups_id`,`foundation_id`,`outlet_id`,`ledger_form_parameter_id`) VALUES
('Partner Commission',5,b'1','PG#5',created_by,6, 5, 2,outlet_id, 1);
END &&
DELIMITER;
SET FOREIGN_KEY_CHECKS = 1;
