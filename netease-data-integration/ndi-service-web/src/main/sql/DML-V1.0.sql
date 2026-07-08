INSERT INTO project_config
(`config_key`, `config_value`, `namespace`)
VALUES
('redis.ip','azkaban5.jd.163.org','NDI'),
('redis.port','6379','NDI'),
('redis.password','ndionline','NDI'),
('redis.max.total','20','NDI'),
('redis.max.idle','10','NDI'),
('redis.min.idle','2','NDI'),
('mammut.service.address','http://bdmsmeta-online.service.163.org','NDI'),
('mammut.api.key','81e52369-199d-48aa-a417-8a114df10443','NDI'),
('mammut.master.key','5764fc6e-4173-4629-a962-5c4fec17f35a','NDI'),
('meta.server.address','http://metahub.service.163.org','NDI'),
('metahub.appid','3e71d41f38df4e43a048411257d6be3b','NDI'),
('metahub.secret','264dfe24-3620-4e2e-bd19-f751f9e25e09','NDI'),
('ip.white.list','10.160.128.31,10.160.128.32,172.17.0.31,10.165.139.76','NDI'),
('api.white.list','/api/v1/auth,/api/login,/api/logon,/api/check/login,/api/v1/task/client/task,/api/v1/task/taskName,/api/v1/check','NDI');

