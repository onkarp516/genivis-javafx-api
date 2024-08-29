SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE if EXISTS system_config_parameter_tbl;
CREATE TABLE system_config_parameter_tbl (
  id BIGINT AUTO_INCREMENT NOT NULL,
   display_name VARCHAR(255) NULL,
   slug VARCHAR(255) NULL,
   is_label BIT(1) NULL,
   status BIT(1) NULL,
   CONSTRAINT pk_system_config_parameter_tbl PRIMARY KEY (id)
);

INSERT INTO `system_config_parameter_tbl` (`display_name`, `slug`, `is_label`, `status`) VALUES
('Free Quantity','is_free_qty', b'0', b'1'),
('Multi Rate', 'is_multi_rates', b'0', b'1'),
('Multi discount', 'is_multi_discount', b'0', b'1'),
('Level A', 'is_level_a',  b'1', b'1'),
('Level B', 'is_level_b',  b'1', b'1'),
('Level C', 'is_level_c',  b'1', b'1'),
('Disc.% Cal', 'is_discount_first_calculation',  b'0', b'1'),
('Disc.amt/unit', 'is_discount_amount_per_unit',  b'0', b'1'),
('mm-yyyy','mm_yyyy', b'0', b'1'),
('Rate Inclusive','rate_inclusive', b'0', b'1'),
('Decimal Calculation','decimal_calculations', b'1', b'1');
SET FOREIGN_KEY_CHECKS = 1;
