SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS role_access_permissions_tbl;
CREATE TABLE role_access_permissions_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   role_id BIGINT NULL,
   action_mapping_id BIGINT NULL,
   created_by BIGINT NULL,
   created_at datetime NULL,
   status BIT(1) NULL,
   user_actions_id VARCHAR(255) NULL,
   CONSTRAINT pk_role_access_permissions_tbl PRIMARY KEY (id)
);

ALTER TABLE role_access_permissions_tbl ADD CONSTRAINT FK_ROLE_ACCESS_PERMISSIONS_TBL_ON_ACTION_MAPPING FOREIGN KEY (action_mapping_id) REFERENCES access_mapping_tbl (id);

ALTER TABLE role_access_permissions_tbl ADD CONSTRAINT FK_ROLE_ACCESS_PERMISSIONS_TBL_ON_ROLE FOREIGN KEY (role_id) REFERENCES role_tbl (id);

SET FOREIGN_KEY_CHECKS = 1;
