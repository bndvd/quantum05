
CREATE TABLE IF NOT EXISTS `quantum`.`transaction` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `sec_id` INT UNSIGNED NOT NULL,
  `user_id` INT UNSIGNED NOT NULL,
  `tran_date` DATETIME NOT NULL,
  `type` VARCHAR(3) NOT NULL,
  `shares` DOUBLE(20,10) NOT NULL,
  `price` DOUBLE(20,10) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC));

CREATE TABLE IF NOT EXISTS `quantum`.`security` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `basket_id` INT UNSIGNED NOT NULL,
  `symbol` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC));

CREATE TABLE IF NOT EXISTS `quantum`.`basket` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
  `name` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `id_UNIQUE` (`id` ASC));

CREATE TABLE IF NOT EXISTS `quantum`.`keyval` (
  `kvkey` VARCHAR(128) NOT NULL,
  `kvvalue` VARCHAR(1024) NOT NULL,
  PRIMARY KEY (`kvkey`),
  UNIQUE INDEX `kvkey_UNIQUE` (`kvkey` ASC) VISIBLE);
