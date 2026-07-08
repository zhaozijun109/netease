CREATE TABLE `task_develop` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `owner` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '负责人',
  `product` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'product',
  `creator` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '创建人',
  `modifier` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '最近修改人',
  `executor` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT '执行人',
  `task_id` varchar(64) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '任务id',
  `task_name` varchar(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '任务名称',
  `task_description` varchar(1024) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT '任务描述',
  `migration_type` tinyint(4) DEFAULT NULL COMMENT '同步类型',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间',
  `execute_time` timestamp NULL DEFAULT NULL COMMENT '最近执行时间',
  `version` int(11) NOT NULL COMMENT '任务版本',
  `status` tinyint(4) NOT NULL COMMENT '任务当前状态',
  `properties` json DEFAULT NULL COMMENT '任务同步属性',
  `handlers` text NOT NULL COMMENT 'handler信息',
  `reader_id` bigint(20) NOT NULL COMMENT '数据源信息表id',
  `reader_type` tinyint(4) NOT NULL COMMENT '来源表的类型',
  `reader_table_name` varchar(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '来源表表名',
  `writer_id` bigint(20) NOT NULL COMMENT '数据去向信息表id',
  `writer_type` tinyint(4) NOT NULL COMMENT '去向表类型',
  `writer_table_name` varchar(256) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '去向表表名',
  `cluster` varchar(255) NOT NULL COMMENT '集群',
  PRIMARY KEY (`id`),
  UNIQUE KEY `task_name` (`task_name`,`product`,`cluster`,`version`),
  KEY `task_id` (`task_id`),
  KEY `product_cluster_multi` (`product`,`cluster`,`owner`,`reader_type`,`writer_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ALTER TABLE `task_develop` MODIFY COLUMN handlers text NOT NULL COMMENT 'handler信息';
-- ALTER TABLE `task_develop` MODIFY COLUMN `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间', MODIFY COLUMN `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '修改时间'

ALTER TABLE `task_develop` ADD COLUMN `reader_url` text NOT NULL DEFAULT "" COMMENT '来源数据源的url，多数据源通过；分隔';
ALTER TABLE `task_develop` ADD COLUMN `writer_url` VARCHAR(255) NOT NULL DEFAULT "" COMMENT '去向数据源的url';