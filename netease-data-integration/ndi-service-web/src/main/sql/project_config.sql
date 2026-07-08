create table `project_config` (
    `id` bigint not null AUTO_INCREMENT,
    `config_key` varchar(255) not null,
    `config_value` varchar(1024) not null,
    `namespace` varchar(64) not null,
    `create_time` timestamp not null default CURRENT_TIMESTAMP,
    `modify_time` timestamp not null default CURRENT_TIMESTAMP,
    primary key(`id`),
    unique (`config_key`, `namespace`)
)