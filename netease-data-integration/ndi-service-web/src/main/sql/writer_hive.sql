CREATE TABLE `writer_hive` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `data_sources` json NOT NULL COMMENT '数据源信息',
  `insert_type` tinyint(4) NOT NULL COMMENT '数据写入类型：insert overwrite；insert into',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `task_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL,
  `conf` varchar(2048) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

# data_sources create_time, modify_time