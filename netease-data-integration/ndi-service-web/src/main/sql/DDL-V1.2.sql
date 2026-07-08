CREATE TABLE `task_instance`
(
    `id`                    BIGINT      NOT NULL auto_increment,
    `task_id`               VARCHAR(64) NOT NULL COMMENT '任务id',
    `status`                VARCHAR(16) NOT NULL COMMENT '实例运行状态',
    `create_time`           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `modify_time`           TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `operated_row`          INT COMMENT '实例运行操作的数据行数',
    `column_start_position` BIGINT COMMENT '列的开始位置',
    `column_end_position`   BIGINT COMMENT '列的结束位置',
    PRIMARY KEY (`id`)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8mb4 COMMENT ='任务实例表';

ALTER TABLE `reader_mysql`
    ADD COLUMN `transform_type`       TINYINT COMMENT '任务类型',
    ADD COLUMN `refer_column`         VARCHAR(64) COMMENT '参照的列',
    ADD COLUMN `column_initial_value` BIGINT COMMENT '参照列的初始值';

ALTER TABLE `reader_oracle`
    ADD COLUMN `transform_type`       TINYINT COMMENT '任务类型',
    ADD COLUMN `refer_column`         VARCHAR(64) COMMENT '参照的列',
    ADD COLUMN `column_initial_value` BIGINT COMMENT '参照列的初始值';