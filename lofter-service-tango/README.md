# 开发流程

# 快速接入

```
<dependency>
    <groupId>com.netease.lofter.tango</groupId>
    <artifactId>lofter-service-tango-api</artifactId>
    <version>${lofter-tango-version}</version>
</dependency>
<dependency>
    <groupId>com.netease.lofter.tango</groupId>
    <artifactId>lofter-service-tango-integration</artifactId>
    <version>${lofter-tango-version}</version>
</dependency>

配置文件：apollo.bootstrap.namespaces=lofter.tango

```

## 1.建表

### 建表规范

- 表命名规则：建议使用下划线
- 表字段命名规则：建议使用驼峰式
- 表中含有四个字段：id，createTime, updateTime, status
- 表字段注释：协议格式：字段解释(补充解释)[javax包下注解名称]

## 2.修改配置文件生成模板代码

- 执行 ***GeneratorUtils*** 类生成CRUD代码  
  <mark>请自己拷贝一份generator-config.properties文件放到自己电脑某个地方，生成时指向自己的配置文件</mark>  
  目录结构

```
  ├── delegate
  │ └── xxxDelagate.java
  ├── entity
  │ └── xxx.java
  ├── mapper
  │ └── xxxMapper.java
  ├── service
  │ └── xxxService.java
  └── web
      ├── controller
      │ └── xxxController.java
      ├── query
      │ └── xxxQuery.java
      ├── ro
      | └── xxxRO.java
      └── vo
        └── xxxVO.java
  ```

## 3.OX选择分支扫描代码，根据接口声明生成协议

[OX地址](https://music-ox.hz.netease.com/ox/music/app/detail/11638/feature%2Fconfig/api)

## 4.Tango接口从OX导入

## 5.根据模板或者智能导航组件生成可视化CRUD前端代码

## 6.重新选择接口

## 7.调试

# 其它规范

- 请求方法：GET | POST
- 日志：controller路径以update|delete|add|save 结尾的映射并且是POST请求
- 下拉选择 使用vo包中SelectVO
- 响应：Result<T>
- 分页查询参数，继承BaseQuery
- 分页响应：Result<PageResult<T>>
- 请求路径必须/tango开头