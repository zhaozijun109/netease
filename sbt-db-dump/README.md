##### 数据导入任务作业包生成sbt插件

###### 配置
在project/plugins.sbt加入：


    addSbtPlugin("com.netease.wm" % "sbt-db-dump" % "0.0.4")
    
参考采薇书院配置：


    sqoopConnect in generateJob := "jdbc:mysql://10.201.241.43:3307/caiwei-db?user=sta_read&password=tmXsxYAxr"
    jobOutputBaseDirectory in generateJob := "/user/pris/dbdump/youdu"
    jobOutputDirectoryMapper in generateJob := { table: String => snakify(table) }
    jobConnectionsMapper in generateJob := { _ => 5 }
    tableMetaPath in generateJob := { baseDirectory.value / "meta.txt" }
    tableFilter in generateJob := { _: String =>
      true
    }
    tableGroupSize in generateJob := 10
    hiveSchema in generateJob := "youdu"
    hiveTableMapper in generateJob := { table: String => s"ddb_${snakify(table)}"}
    
 
  主要配置有：
   + sqoopConnect 
     
     + 对于ddb的dbi接口：
     
          ip:port?user=username&password=password
     + 其他数据库使用带用户密码的jdbc url
     
   + jobOutputBaseDirectory
   
      数据导入到hdfs的基路径
      
   + jobConnectionsMapper
      
      次函数对于各表配置连接数
       
   + tableMetaPath
   
       业务库元数据位置， 元数据可以通过如下脚本导出（需要修改连接地址）
       
           
            
            dbConnect="mysql -h 10.172.6.5 -P 6000 -u abc --password=123 -A -D yuedu-channel-online"
            printf "["
            tables=""
            for tbl in `$dbConnect -e "show tables" --table|sed '1,3d'|sed '$d'|awk -F ' |' '{print $2}'`;do
                columns=`$dbConnect -e "desc $tbl" --table|sed '1,3d'|sed '$d'|sed 's/[ \t\n]*|[ \t\n]*/|/g'|awk 'BEGIN{FS="|"; ORS=","} NF > 3 {print "{\"columnName\":\"" $2 "\",\"columnType\":\"" $3 "\",\"isPrimaryKey\":" ( length($4) == 0 ? "false" : "true" ) "}" }'|sed s'/.$//'`
                tables="$tables,{\"tableName\":\"$tbl\",\"columns\":[$columns]}"
            done
            tables=`echo $tables|sed s'/^.//'`
            printf $tables
            printf "]\n"

       
   + tableFilter
   
       过滤需要导入的表
       
   + tableGroupSize
   
      导入任务运行分组数量， 默认不分组， 建议设置20（根据yarn队列最大并行任务数量）
      
   + hiveSchema
   
      导入数据hive库名
      
   + hiveTableMapper
   
     生成对应hive库表名映射函数
     

###### 运行
  
  
    sbt generateJob 