# SparkSQL 任务测试框架使用文档

## 概述

本测试框架为 Lofter ETL 项目中的 SparkSQL 任务提供自动化测试能力。框架解决了以下核心问题:

- **任务正确性验证**: 在本地环境中验证 SparkSQL 任务的逻辑正确性,无需依赖生产环境数据
- **快速反馈循环**: 开发者可以在几秒钟内运行测试,发现 SQL 逻辑错误、表依赖问题、分区处理错误等
- **回归测试保护**: 通过单元测试和集成测试保护已有任务,防止重构或升级导致的逻辑破坏
- **批量验证能力**: 能够批量解析和验证项目中全部 1036 个 SparkSQL 任务文件

框架基于 Spark 3.3.2 + Hive Metastore (Derby) + ScalaTest 构建,支持本地运行,具有以下特性:

- 自动解析 `.job` 文件,提取 SQL 查询、依赖关系、Spark 配置
- 自动推断或手动指定表结构 (Mock Schema),创建临时 Hive 表
- 支持 Azkaban 日期变量替换 (`azkaban.flow.N.days.ago`, `azkaban.flow.current.date`)
- 提供 Mock 数据加载、任务执行、结果断言的完整工具链
- 自动跳过 UDF 注册语句,避免测试环境中缺少 UDF 实现的问题
- 支持分区表的定义、数据加载和分区过滤验证

## 目录结构

```
etl/src/test/scala/com/netease/lofter/etl/sparksqltest/
├── README.md                        # 本文档
├── JobFileParser.scala              # .job 文件解析器,提取 SQL、依赖、变量、Spark 配置
├── TableExtractor.scala             # SQL 表引用提取器,分析输入/输出表、分区列
├── VariableSubstitution.scala       # Azkaban 变量替换引擎
├── MockTableSetup.scala             # Mock 表创建工具,自动推断或手动定义表结构
├── SparkSqlTestRunner.scala        # 测试执行引擎,核心数据模型定义
├── SparkSqlJobTestBase.scala       # 测试基类 trait,提供 testJob()、withOutputTable() 等方法
├── UnitTests.scala                  # 单元测试 (JobFileParserTest, TableExtractorTest, VariableSubstitutionTest)
├── BatchJobParseTest.scala          # 批量验证测试,解析全部 1036 个 job 文件
├── ads/
│   └── AdsUserFavoriteTagTopJobTest.scala
├── dimensions/
│   └── DimPostArticleJobTest.scala
├── dwd/
│   └── DwdCollectionDetailJobTest.scala
└── dws/
    └── DwsCreatorBrowseUsersJobTest.scala
```

## 运行测试

### 运行全部测试

```bash
sbt "etl/test"
```

执行全部测试,包括单元测试、批量验证、业务逻辑测试 (22 个测试用例)。

### 按分层运行测试

```bash
# 运行所有 dwd 层测试
sbt "etl/testOnly com.netease.lofter.etl.sparksqltest.dwd.*"

# 运行所有 ads 层测试
sbt "etl/testOnly com.netease.lofter.etl.sparksqltest.ads.*"

# 运行所有 dws 层测试
sbt "etl/testOnly com.netease.lofter.etl.sparksqltest.dws.*"

# 运行所有 dimensions 层测试
sbt "etl/testOnly com.netease.lofter.etl.sparksqltest.dimensions.*"
```

### 运行单个测试类

```bash
sbt "etl/testOnly com.netease.lofter.etl.sparksqltest.ads.AdsUserFavoriteTagTopJobTest"
```

### 仅运行单元测试

```bash
sbt "etl/testOnly com.netease.lofter.etl.sparksqltest.JobFileParserTest com.netease.lofter.etl.sparksqltest.TableExtractorTest com.netease.lofter.etl.sparksqltest.VariableSubstitutionTest"
```

单元测试覆盖 Job 文件解析、表提取、变量替换逻辑,运行速度快 (几秒内完成)。

### 批量验证全部 Job 文件

```bash
sbt "etl/testOnly com.netease.lofter.etl.sparksqltest.BatchJobParseTest"
```

验证 `etl/jobs/` 目录下全部 1036 个 `.job` 文件是否可以成功解析、变量是否可以完整替换、表依赖关系是否正确提取。

## 核心概念

### SparkSqlJob

表示一个已解析的 SparkSQL 任务文件,包含任务的全部元信息:

```scala
case class SparkSqlJob(
    name: String,                  // 任务名称 (文件名去除 .job 后缀)
    filePath: String,              // 任务文件绝对路径
    jobType: String,               // 任务类型,必须为 "sparksql"
    queries: Seq[String],          // SQL 查询语句序列 (从 hive.query.01, hive.query.02... 提取)
    dependencies: Seq[String],     // 依赖的上游任务列表
    sparkConf: Map[String, String],// Spark 配置参数 (从 conf.* 属性提取)
    variables: Set[String]         // SQL 中使用的 Azkaban 变量集合
)
```

由 `JobFileParser.parseJobFile(path)` 解析生成。

### MockData

表示一份要加载到 Hive 表中的 Mock 数据:

```scala
case class MockData(
    database: String,                    // 数据库名 (如 "lofter")
    table: String,                       // 表名 (如 "dwd_post_browse_di")
    data: Seq[Row],                      // 数据行序列 (Spark Row 对象)
    schema: StructType,                  // 表结构 (Spark StructType)
    partitionValues: Map[String, String] // 分区键值对 (通常为空,由 dt 列控制分区)
)
```

通过 `testJob()` 方法的 `mockInputData` 参数传入,框架会自动将数据写入临时 Hive 表。

### MockTableDef

定义一个 Mock 表的结构,用于手动覆盖框架自动推断的表结构:

```scala
case class MockTableDef(
    database: String,               // 数据库名
    table: String,                  // 表名
    columns: Seq[MockColumn],       // 列定义序列
    partitionColumns: Seq[String]   // 分区列名称列表 (如 Seq("dt"))
)
```

通过 `testJob()` 方法的 `schemaOverrides` 参数传入,键为 `"database.table"` 格式。

### MockColumn

定义表的单个列:

```scala
case class MockColumn(
    name: String,                   // 列名
    dataType: DataType,             // Spark 数据类型 (如 StringType, LongType)
    nullable: Boolean = true        // 是否可为空
)
```

支持的数据类型: `StringType`, `IntegerType`, `LongType`, `DoubleType`, `FloatType`, `BooleanType`, `TimestampType`, `DateType`, `DecimalType`, `ArrayType(StringType)`, `MapType(StringType, StringType)`, `BinaryType`。

### SqlAnalysis

SQL 分析结果,包含输入表、输出表、分区列信息:

```scala
case class SqlAnalysis(
    inputTables: Set[TableRef],         // 输入表集合
    outputTable: Option[TableRef],      // 输出表 (单个)
    outputPartitionColumns: Seq[String] // 输出表分区列
)
```

### TableRef

表的唯一引用:

```scala
case class TableRef(
    database: String,               // 数据库名
    table: String                   // 表名
) {
  def fullName: String = s"$database.$table"
}
```

### JobTestResult

任务测试执行结果:

```scala
case class JobTestResult(
    jobName: String,                // 任务名称
    success: Boolean,               // 是否成功执行
    outputTable: Option[String],    // 输出表全名 (database.table)
    outputRowCount: Long,           // 输出行数
    outputColumns: Seq[String],     // 输出列名列表
    outputSample: Seq[Row],         // 输出样本数据 (最多 20 行)
    error: Option[String]           // 错误信息 (失败时)
)
```

### ExpectedOutput

期望输出定义,用于自动验证输出结果:

```scala
case class ExpectedOutput(
    minRows: Option[Int] = None,                // 最少行数
    maxRows: Option[Int] = None,                // 最多行数
    exactRowCount: Option[Int] = None,          // 精确行数
    requiredColumns: Seq[String] = Seq.empty,   // 必需包含的列
    assertions: Seq[DataFrame => Unit] = Seq.empty // 自定义断言函数
)
```

传入 `testJob()` 方法的 `expected` 参数,框架会自动执行验证,验证失败会抛出异常。

## 编写新测试

### 测试编写完整步骤

假设你要测试一个任务 `dws/dws_post_daily_summary_di.job`,该任务从 `lofter.dwd_post_browse_di` 聚合每日帖子浏览统计。

#### 步骤 1: 创建测试类

在 `etl/src/test/scala/com/netease/lofter/etl/sparksqltest/dws/` 目录下创建 `DwsPostDailySummaryJobTest.scala`:

```scala
package com.netease.lofter.etl.sparksqltest.dws

import com.netease.lofter.etl.sparksqltest._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import java.time.LocalDate

class DwsPostDailySummaryJobTest extends SparkSqlJobTestBase {
  
  test("dws_post_daily_summary_di aggregates daily post browse counts") {
    // 测试逻辑将在这里编写
  }
}
```

#### 步骤 2: 定义输入表 Schema

根据任务的输入表 `lofter.dwd_post_browse_di` 定义结构:

```scala
val browseSchema = StructType(Seq(
  StructField("postId", LongType),
  StructField("userId", LongType),
  StructField("occurTime", LongType),
  StructField("deviceOs", StringType),
  StructField("dt", StringType)
))
```

#### 步骤 3: 准备 Mock 数据

创建测试数据,确保 `dt` 列的值与变量替换后的日期一致:

```scala
// 默认 baseDate = 2024-01-02, 所以 azkaban.flow.1.days.ago = 2024-01-01
val browseData = Seq(
  Row(1001L, 100L, 1704067200000L, "iOS", "2024-01-01"),
  Row(1001L, 101L, 1704067300000L, "Android", "2024-01-01"),
  Row(1002L, 102L, 1704067400000L, "iOS", "2024-01-01"),
  Row(1001L, 103L, 1704067500000L, "iOS", "2024-01-01"),
  // 不同日期的数据应被过滤 (如果任务只处理 2024-01-01)
  Row(1003L, 104L, 1704153600000L, "iOS", "2024-01-02")
)
```

#### 步骤 4: 定义输出表 Schema

根据任务的输出表 `lofter.dws_post_daily_summary_di` 定义结构:

```scala
val outputSchema = StructType(Seq(
  StructField("postId", LongType),
  StructField("browse_count", LongType),
  StructField("unique_user_count", LongType),
  StructField("dt", StringType)
))
```

#### 步骤 5: 设置 Schema Override (必需)

明确指定输入输出表的结构和分区列:

```scala
val schemaOverrides = Map(
  "lofter.dwd_post_browse_di" -> MockTableDef(
    "lofter", 
    "dwd_post_browse_di",
    browseSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq,
    Seq("dt")  // 分区列
  ),
  "lofter.dws_post_daily_summary_di" -> MockTableDef(
    "lofter",
    "dws_post_daily_summary_di",
    outputSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq,
    Seq("dt")  // 分区列
  )
)
```

#### 步骤 6: 调用 testJob() 执行测试

```scala
val result = testJob(
  "dws/dws_post_daily_summary_di.job",
  mockInputData = Seq(
    MockData("lofter", "dwd_post_browse_di", browseData, browseSchema)
  ),
  schemaOverrides = schemaOverrides
)
```

#### 步骤 7: 验证输出结果

使用框架提供的断言方法和自定义断言:

```scala
printJobReport(result)
assertJobSuccess(result)
assertJobProducesRows(result, minRows = 1)

withOutputTable(result) { df =>
  // 验证行数
  df.count() shouldBe 2
  
  // 验证 postId=1001 的聚合结果
  val post1001 = df.filter("postId = 1001").collect()(0)
  post1001.getLong(1) shouldBe 3L  // browse_count
  post1001.getLong(2) shouldBe 3L  // unique_user_count
  
  // 验证 postId=1002 的聚合结果
  val post1002 = df.filter("postId = 1002").collect()(0)
  post1002.getLong(1) shouldBe 1L
  post1002.getLong(2) shouldBe 1L
  
  // 验证 postId=1003 不在输出中 (不同日期被过滤)
  val post1003Count = df.filter("postId = 1003").count()
  post1003Count shouldBe 0
}
```

#### 完整示例代码

```scala
package com.netease.lofter.etl.sparksqltest.dws

import com.netease.lofter.etl.sparksqltest._
import org.apache.spark.sql.Row
import org.apache.spark.sql.types._
import java.time.LocalDate

class DwsPostDailySummaryJobTest extends SparkSqlJobTestBase {
  
  test("dws_post_daily_summary_di aggregates daily post browse counts") {
    val browseSchema = StructType(Seq(
      StructField("postId", LongType),
      StructField("userId", LongType),
      StructField("occurTime", LongType),
      StructField("deviceOs", StringType),
      StructField("dt", StringType)
    ))
    
    val browseData = Seq(
      Row(1001L, 100L, 1704067200000L, "iOS", "2024-01-01"),
      Row(1001L, 101L, 1704067300000L, "Android", "2024-01-01"),
      Row(1002L, 102L, 1704067400000L, "iOS", "2024-01-01"),
      Row(1001L, 103L, 1704067500000L, "iOS", "2024-01-01"),
      Row(1003L, 104L, 1704153600000L, "iOS", "2024-01-02")
    )
    
    val outputSchema = StructType(Seq(
      StructField("postId", LongType),
      StructField("browse_count", LongType),
      StructField("unique_user_count", LongType),
      StructField("dt", StringType)
    ))
    
    val result = testJob(
      "dws/dws_post_daily_summary_di.job",
      mockInputData = Seq(
        MockData("lofter", "dwd_post_browse_di", browseData, browseSchema)
      ),
      schemaOverrides = Map(
        "lofter.dwd_post_browse_di" -> MockTableDef(
          "lofter", 
          "dwd_post_browse_di",
          browseSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq,
          Seq("dt")
        ),
        "lofter.dws_post_daily_summary_di" -> MockTableDef(
          "lofter",
          "dws_post_daily_summary_di",
          outputSchema.fields.map(f => MockColumn(f.name, f.dataType)).toSeq,
          Seq("dt")
        )
      )
    )
    
    printJobReport(result)
    assertJobSuccess(result)
    assertJobProducesRows(result, minRows = 1)
    
    withOutputTable(result) { df =>
      df.count() shouldBe 2
      
      val post1001 = df.filter("postId = 1001").collect()(0)
      post1001.getLong(1) shouldBe 3L
      post1001.getLong(2) shouldBe 3L
      
      val post1002 = df.filter("postId = 1002").collect()(0)
      post1002.getLong(1) shouldBe 1L
      post1002.getLong(2) shouldBe 1L
      
      df.filter("postId = 1003").count() shouldBe 0
    }
  }
}
```

### 测试编写最佳实践

1. **数据边界覆盖**: Mock 数据应覆盖正常情况、边界条件、应被过滤的异常数据
2. **分区对齐**: 确保 Mock 数据的 `dt` 列值与变量替换后的日期一致,否则分区过滤会导致数据为空
3. **Schema 完整性**: 在 `schemaOverrides` 中明确指定全部输入输出表的结构,避免框架自动推断不准确
4. **最小化依赖**: 只 Mock 任务实际依赖的输入表,减少测试复杂度
5. **精确断言**: 使用 `withOutputTable()` 访问输出 DataFrame,执行精确的数据验证,不要只检查行数

## API 参考

### SparkSqlJobTestBase Trait

所有业务逻辑测试类应继承此 trait,它提供了测试框架的全部核心方法。

#### testJob()

执行一个 SparkSQL 任务的完整测试,返回测试结果:

```scala
def testJob(
    jobFile: String,                              // 任务文件相对路径 (相对于 etl/jobs/)
    mockInputData: Seq[MockData] = Seq.empty,     // Mock 输入数据
    schemaOverrides: Map[String, MockTableDef] = Map.empty, // Schema 覆盖,键为 "database.table"
    baseDate: LocalDate = defaultBaseDate,        // 基准日期 (默认 2024-01-02)
    expected: ExpectedOutput = ExpectedOutput()   // 期望输出定义
): JobTestResult
```

**功能**:

1. 解析 `jobFile` 指定的 `.job` 文件
2. 提取 SQL 查询、表依赖、变量
3. 根据 `schemaOverrides` 或自动推断创建 Mock 表
4. 加载 `mockInputData` 到对应表中
5. 使用 `baseDate` 替换 Azkaban 变量
6. 执行全部 SQL 查询
7. 收集输出表数据和统计信息
8. 根据 `expected` 验证输出 (如果指定)
9. 返回 `JobTestResult` 对象

**示例**:

```scala
val result = testJob(
  "dwd/dwd_post_browse_di.job",
  mockInputData = Seq(mockData1, mockData2),
  schemaOverrides = Map(
    "lofter.input_table" -> inputTableDef,
    "lofter.output_table" -> outputTableDef
  ),
  baseDate = LocalDate.of(2024, 1, 15),
  expected = ExpectedOutput(minRows = Some(10))
)
```

#### dryRunJob()

解析和分析任务,但不实际执行 SQL,用于调试和检查:

```scala
def dryRunJob(
    jobFile: String,                       // 任务文件相对路径
    baseDate: LocalDate = defaultBaseDate  // 基准日期
): (SparkSqlJob, SqlAnalysis, String)
```

**返回值**:

- `SparkSqlJob`: 解析后的任务对象
- `SqlAnalysis`: SQL 分析结果 (输入表、输出表、分区列)
- `String`: 变量替换后的完整 SQL 文本

**示例**:

```scala
val (job, analysis, substitutedSql) = dryRunJob("dws/dws_creator_daily_stats_di.job")
println(s"Input tables: ${analysis.inputTables.map(_.fullName).mkString(", ")}")
println(s"Output table: ${analysis.outputTable.map(_.fullName).getOrElse("none")}")
println(s"Partition columns: ${analysis.outputPartitionColumns.mkString(", ")}")
```

#### mockTable()

便捷方法,快速创建 `MockData` 对象:

```scala
def mockTable(
    database: String,                      // 数据库名
    table: String,                         // 表名
    columns: Seq[(String, DataType)],      // 列定义 (列名, 数据类型) 元组序列
    data: Seq[Seq[Any]],                   // 数据行,每行是值的序列
    partitionColumns: Seq[String] = Seq.empty // 分区列名称
): MockData
```

**示例**:

```scala
val mockData = mockTable(
  "lofter",
  "dim_user",
  Seq(
    ("userId", LongType),
    ("nickname", StringType),
    ("createTime", LongType)
  ),
  Seq(
    Seq(100L, "user_a", 1704067200000L),
    Seq(101L, "user_b", 1704067300000L)
  )
)
```

#### withOutputTable()

访问任务输出表的 DataFrame,在闭包中执行自定义验证:

```scala
def withOutputTable[T](result: JobTestResult)(f: DataFrame => T): T
```

**前置条件**:

- `result.success` 必须为 `true`
- `result.outputTable` 必须非空

**示例**:

```scala
withOutputTable(result) { df =>
  df.count() shouldBe 100
  df.filter("userId = 1001").count() shouldBe 1
  
  val firstRow = df.orderBy("occurTime").first()
  firstRow.getLong(0) shouldBe 1001L
  firstRow.getString(1) shouldBe "expected_value"
}
```

#### assertJobSuccess()

断言任务执行成功,失败时抛出异常:

```scala
def assertJobSuccess(result: JobTestResult): Unit
```

**示例**:

```scala
assertJobSuccess(result)
```

#### assertJobProducesRows()

断言任务输出至少包含指定行数:

```scala
def assertJobProducesRows(result: JobTestResult, minRows: Int = 1): Unit
```

**示例**:

```scala
assertJobProducesRows(result, minRows = 50)
```

#### assertJobHasColumns()

断言任务输出包含指定的列:

```scala
def assertJobHasColumns(result: JobTestResult, columns: Seq[String]): Unit
```

**示例**:

```scala
assertJobHasColumns(result, Seq("userId", "postId", "occurTime", "dt"))
```

#### printJobReport()

打印任务测试报告到控制台,包含状态、输出表、行数、列、样本数据:

```scala
def printJobReport(result: JobTestResult): Unit
```

**示例输出**:

```
=== Job Test Report: dwd_post_browse_di ===
  Status:      PASS
  Output:      lofter.dwd_post_browse_di
  Row count:   245
  Columns:     postId, userId, occurTime, deviceOs, dt
  Sample rows:
    [1001,100,1704067200000,iOS,2024-01-01]
    [1002,101,1704067300000,Android,2024-01-01]
    [1003,102,1704067400000,iOS,2024-01-01]
==================================================
```

## 分区表处理

### 定义分区表

通过 `MockTableDef` 的 `partitionColumns` 参数指定分区列:

```scala
MockTableDef(
  "lofter",
  "dwd_post_browse_di",
  Seq(
    MockColumn("postId", LongType),
    MockColumn("userId", LongType),
    MockColumn("occurTime", LongType),
    MockColumn("dt", StringType)  // dt 列必须包含在 columns 中
  ),
  Seq("dt")  // 指定 dt 为分区列
)
```

**注意**: 分区列 `dt` 必须同时出现在 `columns` 列表和 `partitionColumns` 列表中。

### 加载分区数据

在 Mock 数据中,分区列作为普通列包含在数据行中:

```scala
val browseData = Seq(
  Row(1001L, 100L, 1704067200000L, "2024-01-01"),  // dt 列在最后
  Row(1002L, 101L, 1704067300000L, "2024-01-01"),
  Row(1003L, 102L, 1704067400000L, "2024-01-02")
)

MockData("lofter", "dwd_post_browse_di", browseData, browseSchema)
```

框架会自动根据 `MockTableDef` 的 `partitionColumns` 配置创建分区表结构,并将数据写入对应分区。

### 分区过滤验证

任务 SQL 中通常包含分区过滤条件 (如 `where dt = '${azkaban.flow.1.days.ago}'`),确保 Mock 数据的 `dt` 值与替换后的日期一致:

```scala
// 默认 baseDate = 2024-01-02
// ${azkaban.flow.1.days.ago} 替换为 2024-01-01
val browseData = Seq(
  Row(1001L, 100L, 1704067200000L, "2024-01-01"),  // 会被查询命中
  Row(1002L, 101L, 1704153600000L, "2024-01-02")   // 会被分区过滤跳过
)
```

如果 Mock 数据的 `dt` 值与 SQL 中的分区条件不匹配,查询结果将为空,导致测试失败。

## 变量替换

框架支持 Azkaban 日期变量的自动替换,无需在测试中手动处理。

### 支持的变量

#### azkaban.flow.N.days.ago

表示相对于 `baseDate` 往前 N 天的日期:

```scala
// baseDate = 2024-01-02
${azkaban.flow.1.days.ago}   // 替换为 2024-01-01
${azkaban.flow.7.days.ago}   // 替换为 2023-12-26
${azkaban.flow.30.days.ago}  // 替换为 2023-12-03
```

支持 N 从 1 到 365。

#### azkaban.flow.current.date

表示 `baseDate` 当天的日期:

```scala
// baseDate = 2024-01-02
${azkaban.flow.current.date}  // 替换为 2024-01-02
```

### 默认基准日期

框架默认使用 `LocalDate.of(2024, 1, 2)` 作为基准日期,可以通过 `testJob()` 方法的 `baseDate` 参数覆盖:

```scala
val result = testJob(
  "dws/dws_post_daily_stats_di.job",
  baseDate = LocalDate.of(2024, 6, 15)
)
```

### 日期格式

替换后的日期格式为 `yyyy-MM-dd`,例如 `2024-01-01`。

### 未知变量处理

如果 SQL 中包含框架不支持的变量 (如 `${custom.var}`),变量会保持原样不替换。批量验证测试会检测这类未解析变量并报错。

## 常见问题

### Jackson ClassNotFoundException

**症状**: 运行测试时报错 `java.lang.ClassNotFoundException: com.fasterxml.jackson.databind.ObjectMapper` 或类似 Jackson 相关错误。

**原因**: Spark 依赖 Jackson 库,但 SBT 默认的 classloader 隔离策略导致测试运行时找不到 Jackson 类。

**解决方案**: 已在 `etl/build.sbt` 中配置以下设置:

```scala
Test / classLoaderLayeringStrategy := ClassLoaderLayeringStrategy.Flat
```

此配置使测试运行时使用扁平化的 classloader,确保 Jackson 类可访问。如果问题仍存在,检查 `build.sbt` 中 `libraryDependencies` 是否包含 `jackson-databind` 的 `test` scope 依赖。

### Derby SecurityManager Error

**症状**: 运行测试时报错 `java.lang.SecurityException: sealing violation: package org.apache.derby.impl.jdbc is sealed` 或 Derby Metastore 初始化失败。

**原因**: Derby 数据库在某些 JDK 版本下受安全策略限制。

**解决方案**: 已在 `etl/build.sbt` 中配置:

```scala
Test / javaOptions ++= Seq(
  "-Djava.security.policy=test.policy",
  "-Xmx2g"
)
```

并在 `etl/test.policy` 文件中授予全部权限:

```
grant {
  permission java.security.AllPermission;
};
```

确保 `test.policy` 文件存在于 `etl/` 目录下。

### UDF 注册失败

**症状**: 测试执行时跳过 UDF 注册语句,控制台输出警告:

```
[WARN] Skipped 1 UDF registration query(s) for job xxx
```

**原因**: 任务 SQL 中包含 `CREATE TEMPORARY FUNCTION` 语句注册 UDF,但测试环境中缺少对应的 UDF 实现 (Java/Scala 类或 JAR 文件)。

**解决方案**: 框架会自动跳过 UDF 注册语句,继续执行后续 SQL。这是正常行为,不会影响测试。如果任务逻辑严重依赖 UDF,需要:

1. 将 UDF 实现添加到测试依赖中
2. 在测试类的 `beforeAll` 方法中手动注册 UDF

```scala
override def beforeAll(): Unit = {
  super.beforeAll()
  spark.udf.register("my_udf", (input: String) => {
    // UDF 实现
    input.toUpperCase
  })
}
```

### 反引号表名无法识别

**症状**: 任务 SQL 中使用反引号包裹表名 (如 `` `lofter`.`dim_post` ``),框架无法正确提取表引用。

**原因**: `TableExtractor` 的正则表达式已支持反引号语法。

**解决方案**: 无需修改,框架已处理此情况。如果仍遇到问题,检查表名是否符合以下格式之一:

- `lofter.table_name`
- `` `lofter`.`table_name` ``
- `lofter_db_dump.table_name`
- `lofter_dm.table_name`

### 分区数据未出现在输出中

**症状**: 测试中加载了 Mock 数据,但任务执行后输出为空或行数不符合预期。

**原因**: Mock 数据的 `dt` 列值与任务 SQL 中的分区过滤条件不匹配。

**解决方案**:

1. 确认任务 SQL 中分区过滤条件使用的变量,例如 `where dt = '${azkaban.flow.1.days.ago}'`
2. 计算替换后的日期值 (默认 `baseDate = 2024-01-02`, `azkaban.flow.1.days.ago = 2024-01-01`)
3. 确保 Mock 数据的 `dt` 列值与替换后的日期一致:

```scala
val mockData = Seq(
  Row(1001L, 100L, 1704067200000L, "2024-01-01")  // dt 必须是 2024-01-01
)
```

4. 如果需要测试其他日期,通过 `baseDate` 参数指定:

```scala
val result = testJob(
  "dwd/dwd_post_browse_di.job",
  baseDate = LocalDate.of(2024, 6, 15),  // azkaban.flow.1.days.ago = 2024-06-14
  mockInputData = Seq(mockDataWithDt_2024_06_14)
)
```

### 输出表结构不匹配

**症状**: 测试失败,报错 `AnalysisException: Cannot insert into table ... due to data type mismatch` 或类似错误。

**原因**: `schemaOverrides` 中定义的输出表结构与任务 SQL 中 `INSERT OVERWRITE` 语句的 SELECT 子句字段类型不匹配。

**解决方案**:

1. 查看任务 SQL 中的 SELECT 子句,确定输出字段的类型
2. 在 `MockTableDef` 中使用完全匹配的类型定义输出表:

```scala
// 任务 SQL: SELECT userId, COUNT(*) as cnt, MAX(occurTime) as last_time
val outputSchema = StructType(Seq(
  StructField("userId", LongType),       // 匹配 userId 类型
  StructField("cnt", LongType),          // COUNT(*) 返回 LongType
  StructField("last_time", LongType),    // MAX(occurTime) 返回 LongType
  StructField("dt", StringType)
))
```

### 测试运行缓慢

**症状**: 单个测试运行时间超过 30 秒。

**原因**: Spark 初始化、Hive Metastore 初始化、分区表操作都会消耗时间。

**解决方案**:

1. 使用 `sbt "etl/testOnly <TestClass>"` 运行单个测试类,避免重复初始化
2. 减少 Mock 数据量,通常 10-100 行数据足以验证逻辑
3. 在 `etl/build.sbt` 中已配置 `-Xmx2g` 增加堆内存,确保 JVM 有足够内存
4. 首次运行会较慢 (Spark session 初始化),后续运行会复用 session

### 批量验证报告大量失败

**症状**: 运行 `BatchJobParseTest` 时,报告显示大量任务解析失败或表提取失败。

**原因**:

1. 任务文件格式不符合框架预期 (非 `type=sparksql` 任务会被跳过)
2. SQL 语法使用了框架 `TableExtractor` 不支持的模式 (如子查询、复杂 CTE)

**解决方案**:

1. 检查失败任务列表,确认是否为非 SparkSQL 任务 (如 Java/Scala 任务)
2. 对于复杂 SQL 语法,框架仍能执行任务,只是表提取可能不完整,不影响实际测试
3. 如果关键任务解析失败,可以手动检查 SQL 语法,确保符合 Hive SQL 规范

## 现有测试用例

### 单元测试 (UnitTests.scala)

**JobFileParserTest** (6 个测试):

1. `parse simple sparksql job`: 解析包含单个查询、依赖的基本 SparkSQL 任务
2. `parse multiline query with backslash continuation`: 解析使用反斜杠换行的多行 SQL 查询
3. `extract azkaban variables`: 从 SQL 中提取 `${azkaban.flow.N.days.ago}` 变量
4. `parse spark conf properties`: 解析 `conf.spark.*` 配置参数
5. `skip non-sparksql jobs`: 跳过非 SparkSQL 类型的任务 (如 Java Jar 任务)
6. `handle empty dependencies`: 正确处理没有依赖的任务

**TableExtractorTest** (4 个测试):

1. `extract input and output tables`: 从 SQL 中提取输入表集合和输出表,排除输出表出现在输入集合中
2. `extract partition columns`: 从 `PARTITION(dt = ...)` 子句中提取分区列
3. `handle CTE queries`: 正确处理 `WITH ... AS` 公共表表达式,提取 CTE 内部的表引用
4. `handle multiple database prefixes`: 支持 `lofter`, `lofter_db_dump`, `lofter_dm` 三个数据库前缀

**VariableSubstitutionTest** (5 个测试):

1. `substitute azkaban date variables`: 替换 `${azkaban.flow.1.days.ago}` 为具体日期
2. `substitute multiple date ranges`: 在同一 SQL 中替换多个不同天数的日期变量
3. `substitute current date`: 替换 `${azkaban.flow.current.date}` 为当前基准日期
4. `leave unknown variables unchanged`: 保持未知变量原样不替换
5. `extract all variables from sql`: 提取 SQL 中全部 `${...}` 变量

### 批量验证测试 (BatchJobParseTest.scala)

**BatchJobParseTest** (3 个测试):

1. `all sparksql job files parse successfully`: 解析 `etl/jobs/` 目录下全部 1036 个 `.job` 文件,验证解析成功率和表提取正确性
2. `all sparksql jobs have valid variable substitution`: 验证全部任务的变量替换后无剩余未解析变量
3. `report table dependency graph`: 统计全部任务的输入输出表依赖关系,生成数据血缘报告

### 业务逻辑测试

**DimPostArticleJobTest** (dimensions 层,1 个测试):

- `dim_post_article filters out questions from dim_post`: 验证维度表过滤逻辑,从 `dim_post` 中排除 `contentType = "问答"` 的帖子,保留图片、视频等内容类型

**DwdCollectionDetailJobTest** (dwd 层,1 个测试):

- `dwd_collection_detail_di filters collection browse events`: 验证明细层过滤逻辑,从内容浏览事件中筛选 `item_type = "合集"` 且 `itemId > 0` 的记录,验证分区过滤 (只保留 `dt = 2024-01-01` 的数据)

**DwsCreatorBrowseUsersJobTest** (dws 层,1 个测试):

- `dws_creator_browse_users_di aggregates browse events per creator-user pair`: 验证汇总层 `GROUP BY` 聚合逻辑,按创作者-用户对统计首次和最后一次浏览时间 (`MIN`, `MAX`),过滤 `is_real > 0` 的真实用户

**AdsUserFavoriteTagTopJobTest** (ads 层,1 个测试):

- `ads_par_user_favorite_tag_top_dd computes top-9 favorite tags per user`: 验证应用层窗口函数逻辑,使用 `row_number() OVER (PARTITION BY userId ORDER BY SUM(pv) DESC)` 计算每个用户的 Top 9 喜爱标签,过滤 180 天时间窗口 (`date_sub('${azkaban.flow.1.days.ago}', 180)`),跨库分区写入 (`lofter_dm` 数据库)

全部 22 个测试用例覆盖了框架核心功能、批量验证能力、以及 4 个数仓分层 (维度层、明细层、汇总层、应用层) 的典型业务逻辑模式。
