create table `service_info`(
    `id` bigint not null auto_increment,
    `server_name` varchar(16) not null,
    `secret` varchar(64) not null,
    `create_time` timestamp not null default current_timestamp,
    `modify_time` timestamp not null default current_timestamp,
    primary key (`id`),
    unique (`server_name`)
)