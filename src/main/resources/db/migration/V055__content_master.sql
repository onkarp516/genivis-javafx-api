SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS content_master_tbl;
CREATE TABLE content_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   content_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_content_master_tbl PRIMARY KEY (id)
);
DROP TABLE if EXISTS content_pkg_master_tbl;
CREATE TABLE content_pkg_master_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   content_package_name VARCHAR(255) NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_content_pkg_master_tbl PRIMARY KEY (id)
);
INSERT INTO content_pkg_master_tbl (
  content_package_name,
  status
) VALUES
  (
    'mg',
    1
  ),
  (
    'ml',
    1
  );
  DROP TABLE if EXISTS commission_master_tbl;
  CREATE TABLE commission_master_tbl (
    id BIGINT AUTO_INCREMENT NOT NULL,
     role_type VARCHAR(255) NULL,
     franchise_level VARCHAR(255) NULL,
     product_level VARCHAR(255) NULL,
     status BIT(1) NULL,
     CONSTRAINT pk_commission_master_tbl PRIMARY KEY (id)
  );

SET FOREIGN_KEY_CHECKS = 1;