CREATE TABLE `reader_hive`
(
  `id`          bigint(20) NOT NULL AUTO_INCREMENT,
  `data_source` json       NOT NULL COMMENT '数据源的信息',
  `conditions`  varchar(4096)       DEFAULT NULL COMMENT '过滤条件',
  `create_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `conf`        varchar(4096)       DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `reader_ddb_dbi`
(
  `id`           bigint(20) NOT NULL AUTO_INCREMENT,
  `data_sources` json       NOT NULL COMMENT '数据源的信息',
  `conditions`   varchar(4096)       DEFAULT NULL COMMENT '过滤条件',
  `create_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `conf`         varchar(4096)       DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `reader_ddb_qs`
(
  `id`           bigint(20) NOT NULL AUTO_INCREMENT,
  `data_sources` json       NOT NULL COMMENT '数据源的信息',
  `conditions`   varchar(4096)       DEFAULT NULL COMMENT '过滤条件',
  `create_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `conf`         varchar(4096)       DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

CREATE TABLE `writer_mysql`
(
  `id`          bigint(20) NOT NULL AUTO_INCREMENT,
  `data_source` json       NOT NULL COMMENT '数据源信息',
  `insert_type` tinyint(4) NOT NULL COMMENT '数据写入类型：insert overwrite；insert into',
  `create_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `pre_sql`     varchar(4096)       DEFAULT '' COMMENT '预处理SQL，JSON格式化的String数组',
  `post_sql`    varchar(4096)       DEFAULT '' COMMENT '后处理SQL，JSON格式化的String数组',
  `conf`        varchar(4096)       DEFAULT NULL COMMENT '配置信息，JSON格式化的String',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
CREATE TABLE `writer_ddb_dbi`
(
  `id`          bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `data_source` json       NOT NULL COMMENT '数据源信息',
  `insert_type` tinyint(4) NOT NULL COMMENT '数据写入类型',
  `pre_sql`     varchar(4096)       DEFAULT '' COMMENT '预处理SQL，JSON格式化的String数组',
  `post_sql`    varchar(4096)       DEFAULT '' COMMENT '后处理SQL，JSON格式化的String数组',
  `conf`        varchar(4096)       DEFAULT NULL COMMENT '配置信息，JSON格式化的String',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='DDB_DBI Writer表';
CREATE TABLE `writer_ddb_qs`
(
  `id`          bigint(20) NOT NULL AUTO_INCREMENT,
  `create_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `data_source` json       NOT NULL COMMENT '数据源信息',
  `insert_type` tinyint(4) NOT NULL COMMENT '数据写入类型',
  `pre_sql`     varchar(4096)       DEFAULT '' COMMENT '预处理SQL，JSON格式化的String数组',
  `post_sql`    varchar(4096)       DEFAULT '' COMMENT '后处理SQL，JSON格式化的String数组',
  `conf`        varchar(4096)       DEFAULT NULL COMMENT '配置信息，JSON格式化的String',
  PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='DDB_QS Writer表';

create table `project_config`
(
  `id`           bigint(20)    NOT NULL AUTO_INCREMENT,
  `config_key`   varchar(255)  NOT NULL COMMENT '配置key',
  `config_value` varchar(1024) NOT NULL COMMENT '配置value',
  `namespace`    varchar(64)   NOT NULL COMMENT '配置命名空间',
  `create_time`  timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modify_time`  timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  primary key (`id`),
  unique (`config_key`, `namespace`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='系统配置表';

create table `service_info`
(
  `id`          bigint(20)  NOT NULL AUTO_INCREMENT,
  `server_name` varchar(16) NOT NULL COMMENT '服务名',
  `secret`      varchar(64) NOT NULL COMMENT '服务密码',
  `create_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `modify_time` timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  primary key (`id`),
  unique (`server_name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='服务信息表';

alter table writer_hive change `data_sources` `data_source` json NOT NULL,
    change `create_time` `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
    drop column `task_id`, add `partition_list` varchar(2048) DEFAULT NULL;


alter table reader_mysql change `create_time` `create_time` timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP;

alter table task_develop add `reader_url` varchar(2048) DEFAULT '' COMMENT '来源数据源url，多数据源使用；分隔',
    add `writer_url` varchar(2048) DEFAULT '' COMMENT '去向数据源url，多数据源使用；分隔';

alter table task_online add `reader_url` varchar(2048) DEFAULT '' COMMENT '来源数据源url，多数据源使用；分隔',
    add `writer_url` varchar(2048) DEFAULT '' COMMENT '去向数据源url，多数据源使用；分隔';
