### 文漫产品部工具类
在build.sbt加入：

    libraryDependencies ++= Seq(
     "com.netease.wm" %% "wm-util" % "0.0.9"
     ）

目前有以下工具:

1. html表格生成

    [代码示例](https://g.hz.netease.com/yuedu-statistics/common/wm-util/tree/master/src/main/scala/sample/html.scala)

2. ​excel生成
    
    一个无任何依赖的xlsx格式输出工具，支持的功能：列宽、冻结窗格、单元格格式（数值格式、日期格式）、单元格合并、单元格背景色、边框、对齐、字体（大小、颜色、粗体、斜体）
    
    [代码示例](https://g.hz.netease.com/yuedu-statistics/common/wm-util/tree/master/src/main/scala/sample)

3. sql库
   
    一个基于jdbc的简单scala sql库， 支持按位置和名称设置参数


    Class.forName("org.h2.Driver")
    implicit val conn: Connection = DriverManager.getConnection("jdbc:h2:mem:test")
      
    import Sql._
    Sql.execute("create table book(id varchar(255), title varchar(255))")
    Sql.execute("insert into book values('123', 'abc')")
      
    queryAll[Book]("select * from book")
      
    val b2 = Book("100", "Programming Scala")
    sql"insert into book values(${0}, ${1})".update(param(b2))
    sql"insert into book values(${'id}, ${1})".update(param(b2))
    sql"""insert into book values(${"id"}, ${1})""".update(param(b2))

4. args scala命令行参数解析
   
    scalding风格的命令行参数解析， 建议使用保持一致的风格
    
 
    // 命令行使用
        command --input1 <arg1>  --input2 <arg2> --input3
   
    // 解析代码:
         def main(args: Array[String]): Unit = {
           val pargs = Args(args)
      
           val arg1 = pargs.required("input1")
           val optionalArg2 = pargs.optional("input2")
           val boolArg3 = pargs.boolean("input3")
   
5. mail dsl
 
    使用示例如下：


     import com.netease.wm.util.mail._
  
     send a new Mail (
       from = ("symbiansigned@corp.netease.com", "symbiansigned"),
       to = "hzluodawei@corp.netease.com" :: "hzmaoyinjie@corp.netease.com" :: "hzxiaonaitong@corp.netease.com" :: Nil,
       cc = "cuiqifan@corp.netease.com",
       subject = s"蜗牛H5页面访问量$date",
       message = "详见附件",
       attachment = new java.io.File(resultFile)

