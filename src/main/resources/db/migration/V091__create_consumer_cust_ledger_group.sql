SET FOREIGN_KEY_CHECKS = 0;
DROP PROCEDURE if EXISTS consumer_cust_ledger_group;
DELIMITER &&
CREATE PROCEDURE consumer_cust_ledger_group(IN outlet_id BIGINT,IN created_by BIGINT)
BEGIN
INSERT INTO `associates_groups_tbl` (`associates_name`,`under_id`,`status`,`under_prefix`,`created_by`,`principle_id`,
`principle_groups_id`,`foundation_id`,`outlet_id`,`ledger_form_parameter_id`) VALUES
('Consumer Customer',1,b'1','PG#1',created_by,3, 1, 1,outlet_id, 2);

END &&
DELIMITER ;
SET FOREIGN_KEY_CHECKS = 1;