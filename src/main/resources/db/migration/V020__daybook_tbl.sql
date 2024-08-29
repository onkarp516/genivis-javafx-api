SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS day_book_tbl;
CREATE TABLE day_book_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   tranx_date date NULL,
   particulars VARCHAR(255) NULL,
   voucher_no VARCHAR(255) NULL,
   voucher_type VARCHAR(255) NULL,
   amount DOUBLE NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_day_book_tbl PRIMARY KEY (id)
);

ALTER TABLE day_book_tbl ADD CONSTRAINT FK_DAY_BOOK_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE day_book_tbl ADD CONSTRAINT FK_DAY_BOOK_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;