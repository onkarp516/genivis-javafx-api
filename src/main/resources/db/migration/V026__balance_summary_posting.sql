SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE if EXISTS LEDGER_BALANCE_SUMMARY_POSTINGS_INSERT;
DELIMITER &&
CREATE PROCEDURE LEDGER_BALANCE_SUMMARY_POSTINGS_INSERT(IN foundation_fk BIGINT,IN principle_fk BIGINT, IN principle_groups_fk BIGINT,IN associate_group_fk BIGINT, IN branch_fk BIGINT,IN outlet_fk BIGINT,IN underprefix VARCHAR(255),IN ledger_master_fk BIGINT)
BEGIN
    DECLARE DBOPENING_BAL DOUBLE DEFAULT 0.0;
    DECLARE CHECKEXIST INT;
    DECLARE CHECKBALEXIST INT;
    DECLARE LEDGER_MST_ID BIGINT;
    DECLARE INSTITUTE_ID BIGINT;
    DECLARE BRANCH_ID BIGINT;
    DECLARE OUTLET_ID BIGINT;
    DECLARE DETAIL_ID BIGINT;
    DECLARE CREDIT_AMT DOUBLE DEFAULT 0.0;
    DECLARE DEBIT_AMT DOUBLE DEFAULT 0.0;
    DECLARE CLOSING_BAL_CR DOUBLE DEFAULT 0.0;
    DECLARE CLOSING_BAL_DR DOUBLE DEFAULT 0.0;
    DECLARE CLOSING_BAL DOUBLE DEFAULT 0.0;
    SET LEDGER_MST_ID = ledger_master_fk;
        SELECT ledger_master_tbl.opening_bal INTO DBOPENING_BAL FROM ledger_master_tbl WHERE ledger_master_tbl.id = ledger_master_fk;
        SELECT sum(amount) INTO CLOSING_BAL_CR FROM `ledger_transaction_postings_tbl` WHERE ledger_type ='CR' AND ledger_master_id=ledger_master_fk ORDER BY transaction_date ASC;
        SELECT sum(amount) INTO CLOSING_BAL_DR FROM `ledger_transaction_postings_tbl` WHERE ledger_type ='DR' AND ledger_master_id=ledger_master_fk ORDER BY transaction_date ASC;

   SET CLOSING_BAL = DBOPENING_BAL-CLOSING_BAL_DR+CLOSING_BAL_CR;
SELECT EXISTS (SELECT * FROM ledger_balance_summary_tbl WHERE ledger_balance_summary_tbl.ledger_master_id=LEDGER_MST_ID) INTO CHECKBALEXIST;
    IF CHECKBALEXIST <> 0 THEN
  UPDATE ledger_balance_summary_tbl SET ledger_balance_summary_tbl.balance=CLOSING_BAL,ledger_balance_summary_tbl.closing_bal=CLOSING_BAL WHERE ledger_balance_summary_tbl.ledger_master_id=LEDGER_MST_ID;
  ELSE
  INSERT INTO `ledger_balance_summary_tbl`(`foundation_id`, `principle_id`, `principle_groups_id`, `ledger_master_id`,  `opening_bal`, `closing_bal`, `balance`, `created_at`, `updated_at`, `status`, `branch_id`, `outlet_id`, `associate_groups_id`) VALUES (foundation_fk, principle_fk, principle_groups_fk,LEDGER_MST_ID,DBOPENING_BAL,CLOSING_BAL,CLOSING_BAL,NOW(),NOW(),1,branch_fk,outlet_fk,associate_group_fk);
  END IF;

END &&
DELIMITER ;
SET FOREIGN_KEY_CHECKS = 1;

