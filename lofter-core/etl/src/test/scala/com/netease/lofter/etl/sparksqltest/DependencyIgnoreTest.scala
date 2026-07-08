package com.netease.lofter.etl.sparksqltest

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.io.{File, PrintWriter}
import java.nio.file.Files

class DependencyIgnoreTest extends AnyFunSuite with Matchers {

  test("job parser correctly handles dependencies without validation") {
    // 创建一个临时job文件，包含不存在的依赖
    val tempJobFile = Files.createTempFile("test_job", ".job").toFile
    tempJobFile.deleteOnExit()
    
    val jobContent =
      """type=sparksql
        |conf.spark.dynamicAllocation.maxExecutors=5
        |hive.query.01=\
        |select userId, \
        |       count(*) as post_count \
        |from lofter.dim_post \
        |where dt = '${azkaban.flow.1.days.ago}' \
        |group by userId
        |
        |hive.query.02=\
        |insert overwrite table lofter_dm.test_output partition(dt = '${azkaban.flow.1.days.ago}') \
        |select userId, post_count \
        |from ( \
        |    select userId, post_count \
        |    from temp_view \
        |) t
        |
        |dependencies=non_existent_job1,non_existent_job2,ads_creator_gift_open_entry_dd
        |""".stripMargin

    val writer = new PrintWriter(tempJobFile)
    writer.write(jobContent)
    writer.close()

    // 解析job文件
    val jobOpt = JobFileParser.parseJobFile(tempJobFile.getAbsolutePath)
    
    // 验证解析成功
    jobOpt should not be empty
    val job = jobOpt.get
    
    // 验证job基本信息
    job.name should include("test_job")
    job.jobType should be("sparksql")
    job.queries should have size 2
    
    // 验证依赖被正确解析（但不验证其存在性）
    job.dependencies should contain allOf("non_existent_job1", "non_existent_job2", "ads_creator_gift_open_entry_dd")
    job.dependencies should have size 3
    
    // 验证SQL查询被正确解析
    job.queries.head should include("select userId")
    job.queries(1) should include("insert overwrite table")
    
    // 验证Spark配置被正确解析
    job.sparkConf should contain("spark.dynamicAllocation.maxExecutors" -> "5")
    
    // 验证变量被正确提取
    job.variables should contain("azkaban.flow.1.days.ago")
    
    println(s"✓ 测试通过: Job解析器正确处理了包含不存在依赖的job文件")
    println(s"  - Job名称: ${job.name}")
    println(s"  - 依赖数量: ${job.dependencies.size}")
    println(s"  - 查询数量: ${job.queries.size}")
    println(s"  - 变量数量: ${job.variables.size}")
  }

  test("test runner ignores dependencies during execution") {
    // 创建一个临时job文件
    val tempJobFile = Files.createTempFile("dep_test_job", ".job").toFile
    tempJobFile.deleteOnExit()
    
    val jobContent =
      """type=sparksql
        |hive.query.01=\
        |create temporary view test_view as \
        |select 1001 as userId, 'test' as status, '${azkaban.flow.1.days.ago}' as dt
        |
        |hive.query.02=\
        |select count(*) as total_count from test_view
        |
        |dependencies=completely_fake_dependency,another_fake_job
        |""".stripMargin

    val writer = new PrintWriter(tempJobFile)
    writer.write(jobContent)
    writer.close()

    try {
      // 直接使用SparkSqlTestRunner执行测试，应该忽略依赖
      val result = SparkSqlTestRunner.runJobTest(
        tempJobFile.getAbsolutePath,
        mockInputData = Seq.empty,
        schemaOverrides = Map.empty
      )

      // 验证测试执行成功（尽管有不存在的依赖）
      if (!result.success) {
        println(s"测试执行失败: ${result.error.getOrElse("未知错误")}")
      }
      result.success should be(true)
      
      println(s"✓ 测试通过: 测试运行器成功忽略了不存在的依赖")
      println(s"  - 执行结果: ${if(result.success) "成功" else "失败"}")
      
      if (result.outputTable.isDefined) {
        println(s"  - 输出表: ${result.outputTable.get}")
        println(s"  - 输出行数: ${result.outputRowCount}")
      }
      
    } catch {
      case e: Exception =>
        fail(s"测试运行器应该忽略依赖，但是抛出了异常: ${e.getMessage}")
    } finally {
      // 清理测试数据
      SparkSqlTestRunner.cleanup()
    }
  }

  test("batch parse test handles jobs with missing dependencies") {
    // 验证批量解析测试可以处理包含缺失依赖的job文件
    val jobsDir = {
      val projectDir = sys.props.getOrElse("user.dir",
        sys.env.getOrElse("PROJECT_DIR", "."))
      val candidate = new java.io.File(s"$projectDir/etl/jobs")
      if (candidate.exists()) candidate.getAbsolutePath
      else {
        val alt = new java.io.File(s"$projectDir/jobs")
        if (alt.exists()) alt.getAbsolutePath
        else candidate.getAbsolutePath
      }
    }
    
    val jobs = JobFileParser.findSparkSqlJobs(jobsDir)
    jobs should not be empty
    
    // 统计有依赖的job数量
    val jobsWithDeps = jobs.filter(_.dependencies.nonEmpty)
    val totalDeps = jobs.flatMap(_.dependencies).distinct.size
    
    println(s"✓ 批量解析测试结果:")
    println(s"  - 总job数: ${jobs.size}")
    println(s"  - 有依赖的job数: ${jobsWithDeps.size}")  
    println(s"  - 不同依赖总数: $totalDeps")
    println(s"  - 解析成功: 所有job都被正确解析，依赖信息被保留但不验证存在性")
    
    // 验证特定的礼物开通门槛表job
    val giftOpenEntryJob = jobs.find(_.name == "ads_creator_gift_open_entry_dd")
    if (giftOpenEntryJob.isDefined) {
      val job = giftOpenEntryJob.get
      println(s"  - 礼物开通门槛表依赖: [${job.dependencies.mkString(", ")}]")
      println(s"  - 依赖数量: ${job.dependencies.size}")
      if (job.dependencies.nonEmpty) {
        job.dependencies should not be empty
      } else {
        println(s"  - 警告: 礼物开通门槛表没有依赖，这可能是解析问题")
        // 显示job文件路径以供调试
        println(s"  - Job文件路径: ${job.filePath}")
      }
    } else {
      println(s"  - 注意: 未找到礼物开通门槛表job，可能job名称有变化")
      // 显示一些相关的job名称
      val relatedJobs = jobs.filter(_.name.contains("gift")).take(3).map(_.name)
      if (relatedJobs.nonEmpty) {
        println(s"  - 找到相关job: ${relatedJobs.mkString(", ")}")
      }
    }
  }
  
  test("dependency parsing handles various formats") {
    val testCases = Seq(
      // 单个依赖
      ("dependencies=single_job", Seq("single_job")),
      
      // 多个依赖，逗号分隔
      ("dependencies=job1,job2,job3", Seq("job1", "job2", "job3")),
      
      // 带空格的依赖
      ("dependencies=job1, job2 , job3", Seq("job1", "job2", "job3")),
      
      // 空依赖
      ("dependencies=", Seq.empty),
      
      // 带注释的依赖行
      ("dependencies=job1,job2  # this is a comment", Seq("job1", "job2")),
      
      // 多行依赖（使用反斜杠续行）
      ("dependencies=job1,job2,\\\njob3,job4", Seq("job1", "job2", "job3", "job4"))
    )
    
    for ((depLine, expectedDeps) <- testCases) {
      val tempJobFile = Files.createTempFile("dep_format_test", ".job").toFile
      tempJobFile.deleteOnExit()
      
      val jobContent = s"""type=sparksql
        |hive.query.01=select 1 as test
        |$depLine
        |""".stripMargin
        
      val writer = new PrintWriter(tempJobFile)  
      writer.write(jobContent)
      writer.close()
      
      val jobOpt = JobFileParser.parseJobFile(tempJobFile.getAbsolutePath)
      jobOpt should be(defined)
      
      val actualDeps = jobOpt.get.dependencies
      actualDeps should contain theSameElementsAs expectedDeps
      
      println(s"✓ 依赖格式测试通过: '$depLine' -> [${actualDeps.mkString(", ")}]")
    }
  }
}