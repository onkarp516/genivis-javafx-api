ALTER TABLE franchise_master_tbl
ADD addr_state_id BIGINT NULL,
ADD aadhar_no VARCHAR(255) NULL,
ADD pan_no VARCHAR(255) NULL,
ADD dl1no VARCHAR(255) NULL,
ADD dl2no VARCHAR(255) NULL,
ADD dl3no VARCHAR(255) NULL;

ALTER TABLE franchise_master_tbl ADD CONSTRAINT FK_FRANCHISE_MASTER_TBL_ON_ADDR_STATE FOREIGN KEY (addr_state_id) REFERENCES state_tbl (id);

