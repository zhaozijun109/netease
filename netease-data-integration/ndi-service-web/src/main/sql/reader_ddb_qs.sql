 CREATE TABLE `reader_ddb_qs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `data_sources` json NOT NULL COMMENT '数据源的信息',
  `conditions` varchar(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '过滤条件',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `conf` varchar(4096) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;