# 安装EasyML

用户可本地编译安装master版本的EasyML，或者使用已发布版本



## 本地编译安装

1. 环境

   - JDK: 1.8

   - Scala: 2.11.8

     

2. Git

   ```bash
   git clone https://g.hz.netease.com/dsc_alg/easyml.git
   ```

   

3. 进入源码根目录，执行命令

   ```bash
   cd easyml && mvn -DskipTests clean package
   ```

   编译完成后，在源码根目录`assembly/target`目录下会生成一个发布包：`easyml-dist.tar.gz`

   发布包解压后，根目录下有几个子目录和jar包：

   - conf：系统配置文件
     - easyml.properties
     - hanlp.properties
   - jars：EasyML的jar包 & 依赖jar包
   - easyml-starter.jar：项目启动jar包

   

4. 安装到本地仓库

   ```bash
   mvn install
   ```

   

5. 项目pom.xml引用

   ```xml
   <dependency>
     <groupId>com.netease.easyml</groupId>
     <artifactId>easyml-mllib</artifactId>
     <version>0.3.0-SNAPSHOT</version>
   </dependency>
   ```



## 使用发布版本

1. 启动包获取

   从项目[Releases](https://g.hz.netease.com/dsc_alg/easyml/-/releases)页面下载

   

2. 项目pom.xml引用

   1. Netease maven 仓库配置，从下面两种方法中任选一种

      - 方法1：全局配置

        将[settings.xml](./data/settings.xml)拷贝到~/.m2目录下

      - 方法2：项目pom配置

        ```xml
        <repositories>
          <repository>
            <id>alimaven</id>
            <name>aliyun maven</name>
            <url>http://maven.aliyun.com/nexus/content/groups/public/</url>
            <releases>
              <enabled>true</enabled>
            </releases>
            <snapshots>
              <enabled>false</enabled>
            </snapshots>
          </repository>
          <repository>
            <snapshots>
              <enabled>false</enabled>
            </snapshots>
            <id>central</id>
            <name>repo</name>
            <url>http://mvn.hz.netease.com/artifactory/repo</url>
          </repository>
          <repository>
            <id>snapshots</id>
            <name>snapshots-only</name>
            <url>http://mvn.hz.netease.com/artifactory/snapshots-only</url>
          </repository>
        </repositories>
        ```

        

   2. 依赖引用

      ```xml
      <dependency>
        <groupId>com.netease.easyml</groupId>
        <artifactId>easyml-mllib</artifactId>
        <version>0.3.0-SNAPSHOT</version>
      </dependency>
      ```

      