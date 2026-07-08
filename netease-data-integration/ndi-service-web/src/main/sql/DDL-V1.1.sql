# 数据库建表：
CREATE TABLE `task_datasource`
(
    `id`             bigint(20)  NOT NULL AUTO_INCREMENT,
    `data_source_id` bigint(20)  NOT NULL COMMENT '数据源id',
    `task_id`        varchar(64) NOT NULL COMMENT '任务id',
    `product`        varchar(64) NOT NULL COMMENT '产品账号',
    `cluster`        varchar(64) NOT NULL COMMENT '集群',
    `create_time`    timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`    timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    PRIMARY KEY (`id`),
    KEY `datasource_product_cluster` (`data_source_id`, `product`, `cluster`) USING BTREE,
    KEY `task_id` (`task_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='数据源与任务相关性表';

CREATE TABLE `reader_oracle`
(
    `id`           bigint(20) NOT NULL AUTO_INCREMENT,
    `data_sources` json       NOT NULL COMMENT '数据源的信息',
    `conditions`   varchar(4096)       DEFAULT NULL COMMENT '过滤条件',
    `create_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`  timestamp  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
    `conf`         varchar(4096)       DEFAULT NULL,
    PRIMARY KEY (`id`)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Oracle  Reader表';

CREATE TABLE `writer_oracle`
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
  AUTO_INCREMENT = 3
  DEFAULT CHARSET = utf8mb4 COMMENT ='Oracle Writer表';
CREATE TABLE `datasource_azkaban_connection`
(
    `id`            bigint(20)  NOT NULL AUTO_INCREMENT,
    `datasource_id` bigint(20)  NOT NULL COMMENT '数据源id',
    `product_id`    bigint(20)  NOT NULL COMMENT '产品账号',
    `cluster_id`    varchar(64) NOT NULL COMMENT '集群id',
    `exec_status`   tinyint(4)  NOT NULL DEFAULT '5' COMMENT '检查状态3：正在检查中；5：检查结束',
    `exec_result`   tinyint(4)  NOT NULL DEFAULT '2' COMMENT '检查结果。0：成功；1：部分成功；2：失败',
    `exec_message`  json                 DEFAULT NULL COMMENT '检查信息',
    `create_time`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`   timestamp   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `datasource_id_prouduct_id_cluster_id` (`datasource_id`, `product_id`, `cluster_id`) USING BTREE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4 COMMENT ='Azkaban与数据源连通性信息表';



ALTER TABLE `config` ADD unique(`namespace`,`name`);