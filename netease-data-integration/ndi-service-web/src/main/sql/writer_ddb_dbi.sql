CREATE TABLE `writer_ddb_dbi` (
 `id` bigint(20) NOT NULL AUTO_INCREMENT,
 `data_source` json NOT NULL COMMENT '数据源信息',
 `insert_type` tinyint(4) NOT NULL COMMENT '数据写入类型：insert overwrite；insert into',
 `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 `modify_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
 `pre_sql` varchar(4096) default '' comment '',
 `post_sql` varchar(4096) default '' comment '',
 `conf` varchar(4096) DEFAULT NULL,
 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;