SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS parameter_configuration_tbl;
CREATE TABLE parameter_configuration_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   config_name VARCHAR(255) NULL,
   config_label VARCHAR(255) NULL,
   config_value INT NULL,
   status BIT(1) NULL,
   branch_id BIGINT NULL,
   outlet_id BIGINT NULL,
   system_config_master_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   updated_at datetime NULL,
   updated_by BIGINT NULL,
   CONSTRAINT pk_parameter_configuration_tbl PRIMARY KEY (id)
);

ALTER TABLE parameter_configuration_tbl ADD CONSTRAINT FK_PARAMETER_CONFIGURATION_TBL_ON_BRANCH FOREIGN KEY (branch_id) REFERENCES branch_tbl (id);

ALTER TABLE parameter_configuration_tbl ADD CONSTRAINT FK_PARAMETER_CONFIGURATION_TBL_ON_OUTLET FOREIGN KEY (outlet_id) REFERENCES outlet_tbl (id);

ALTER TABLE parameter_configuration_tbl ADD CONSTRAINT FK_PARAMETER_CONFIGURATION_TBL_ON_SYSTEM_CONFIG_MASTER FOREIGN KEY (system_config_master_id) REFERENCES system_config_parameter_tbl (id);
SET FOREIGN_KEY_CHECKS = 1;