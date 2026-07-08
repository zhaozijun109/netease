# 快速开始

基于不同应用场景，用户可灵活选择API、Json配置文件或者脚本三种方式调用算法库。不同调用方式的优缺点：

- API调用
  - 优点：
    - 灵活
    - 定制化强，实现自定义算子开发
  - 缺点：
    - 需要构建maven工程，打Jar包
    - 不方便维护和在线编辑

- Json配置文件调用
  - 优点：
    - zero-code，只需编辑配置文件
    - 方便维护
  - 缺点：
    - 有一定学习成本
    - 只能表达PipeLine逻辑
- 脚本调用
  - 优点：
    - 灵活
    - 方便维护
  - 缺点：
    - 需要熟悉scala语法



## API使用教程

1. pom导入依赖

   ```xml
<dependency>
     <groupId>com.netease.easyml</groupId>
     <artifactId>easyml-launcher</artifactId>
     <version>0.3.0-SNAPSHOT</version>
     <scope>provided</scope>
   </dependency>
   ```
   
   

2. 编写spark driver代码

   ```scala
   import com.netease.easyml.launcher.EasyMLContext
   import org.apache.spark.sql.SparkSession
   
   val easyMLContext = EasyMLContext.builder()
         .appName(this.getClass.getSimpleName)
         .getOrCreate()
   
   val spark = easyMLContext.getSpark
   
   // your machine learning code here
   
   easyMLContext.stop()
   ```

   


## [配置文件使用教程](./configuration.md)



## 脚本使用教程

EasyML支持运行时编译/执行scala脚本，用户无需配置maven工程打Jar包，降低开发成本，也方便在猛犸等平台上管理脚本。

- 规范
  - 类型必须为**object**
  - 类名必须以**UDScript**或**UDS**结尾
  - 必须有**run**方法，且参数一致

- 示例

  1. 脚本：ExampleUDScript.scala

  ```scala
  import org.apache.spark.sql.SparkSession
  
  /**
   * Created by linjiuning on 2020/9/4.
   */
  object ExampleUDScript extends Serializable {
  
    def add(value: Int): Int = value + 1
  
    def run(spark: SparkSession, args: Array[String]): Unit = {
      import spark.implicits._
      val length = if (args.nonEmpty) args(0).toInt else 4
      val df = spark.sparkContext.makeRDD(0 until (length)).filter(_ > 1).map(add).toDF("id")
      df.show(false)
    }
  }
  ```

  2. 启动包easyml-starter.jar上传猛犸，并设为执行jar
  3. 设置执行类为：com.netease.easyml.launcher.Run
  4. 脚本上传猛犸并添加到依赖jars
  5. 参数：script -s ExampleUDScript.scala