# 常见问题

1. spark.sql写入hive表报错

   ```text
   Caused by: java.lang.IllegalArgumentException: Wrong FS: hdfs://hz-cluster8/user/idc/hive_db/dev.db/tmp_easyml_gender_nickname_prediction/.hive-staging_hive_2020-07-23_20-44-46_454_6662660957942558327-1/-ext-10000/part-00000-5d2f8c47-5ad3-4656-be85-d32e99e28635-c000, expected: hdfs://hz-cluster7
   ```

   

   解决方案：

   1. api方式：

      ```scala
      val context = EasyMLContext.builder()
            .set("fs.defaultFS", "hdfs://hz-cluster8")
            .enableHiveSupport()
            .getOrCreate()
          
      val spark = context.getSpark
      ```

   2. 配置文件方式：

      ```json
      {
      	"env":{
          "fs.defaultFS": "hdfs://hz-cluster8"
        }
      }
      ```

2. lightgbm分布式训练Missing label

   ```
   For classification, label values must start from 0 and increase by 1 to n for each partition.  Missing label 0, unique labels 1
   ```

   解决方案：

   ```scala
   val classifier = new LightGBMClassifier()
   									// generateMissingLabels设为true
           					.setGenerateMissingLabels(true)
   ```

   