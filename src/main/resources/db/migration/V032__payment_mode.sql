SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS payment_mode_tbl;
CREATE TABLE payment_mode_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   payment_mode VARCHAR(255) NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_payment_mode_tbl PRIMARY KEY (id)
);
INSERT INTO `payment_mode_tbl` (`payment_mode`, `status`) VALUES
('Cheque', b'1'),('NEFT', b'1'),('IMPS', b'1'),('Debit cards', b'1'),('UPI', b'1'),
('Credit cards', b'1');

SET FOREIGN_KEY_CHECKS = 1;