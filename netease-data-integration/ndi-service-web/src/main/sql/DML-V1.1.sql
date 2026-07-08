# 新增配置信息SQL：
INSERT INTO `config` (`prop`, `name`, namespace)
VALUES ('{\"mammutIndexUrl\": \"https://bdms-dev.hz.netease.com\"}', 'global', 'fronted'),
       ('{\"hz1\": [\"imagesearch4.photo.163.org:8443\"], \"dev4\": [\"hadoop340.photo.163.org:8443\", \"hadoop339.photo.163.org:8443\"], \"ambari\": [\"imagesearch5.photo.163.org:8443\"]}',
        'clustersUrl', 'azkaban');

INSERT INTO `project_config`(config_key, config_value, namespace)
VALUES ('azkaban.user.name', 'azkaban', 'NDI'),
       ('azkaban.password', 'azkaban', 'NDI');