CREATE TABLE `config` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `prop` json DEFAULT NULL,
  `creat_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `name` varchar(255) NOT NULL,
  `namespace` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;