package com.netease.easyml.uds

import com.holdenkarau.spark.testing.SharedSparkContext
import com.netease.easyml.common.util.SparkUtil
import com.netease.easyml.uds.examples.SQLUDS
import org.apache.spark.sql.SparkSession
import org.scalatest.FunSuite

/**
 * Created by linjiuning on 2022/8/20.
 */
class SQLUDSSuite extends FunSuite with SharedSparkContext {
  lazy val spark: SparkSession = SparkSession.builder().getOrCreate()

  test("sql") {
    val sql =
      """
        |-- 注释
        |create temporary cmd load_feature_dump as "com.netease.easyml.uds.util.LoadFeatureDump";
        |create or replace temporary cmd load_feature_dump as "com.netease.easyml.uds.util.LoadFeatureDump";
        |CREATE OR REPLACE TEMPORARY VIEW a as
        |select '200a2285_2b6b72011' as item_id, '一段狗血爱情故事,祺鑫,翔霖,文轩,泗源,复仇文学 全员恶人 狗血预警' as text;
        |@ transform(
        |  input = a,
        |  params = { "type":"JiebaTokenizer", -- 注释
        |  "inputCol":"text",
        |  "outputCol":"tokens",
        |  "lowercase":true },
        |  as = b
        |);
        |env sleep = 10;
        |CREATE OR REPLACE TEMPORARY VIEW c as
        |select
        |  item_id,
        |  concat_ws(' ', tokens) as text
        |from
        |  b;
        |let time = 'unix_timestamp()';
        |@save(input=b, path="target/tmp/tb", source="parquet", saveMode="overwrite");
        |@load(path="target/tmp/tb", source="parquet");
        |@ print(message = "hdfs://hz-cluster10/user/rec/lofter_hive/ods_lofter_noblog_action_di/day=2022-12-16/part-5-15322-f9745d.gz.parquet");
        |@ show(input = "$cache(input = "c")");
        |@ count(input = "c");
        |@ exit(pred = "1>2");
        |@ uds(type="com.netease.easyml.uds.examples.EchoUDS", message="hello sql");
        |@ rm(path="target/tmp/w2v");
        |@ fit(input="b", params={"type":"NSWord2Vec", "inputCol":"tokens"}, stored="w2v", path="target/tmp/w2v");
        |let pred = "2>3";
        |@ if(pred="${pred}", true_fn="@touch(path="target/tmp/w2v/done")");
        |@ for(var="i", values="$(concat_ws(',', array(1,2,3)))", fn="@ print(message="for ${i}")");
        |@ sleep(secs=${sleep});
        |@ until(pred="unix_timestamp() > ${time} + 10", fn="@ print(message="until finished")", secs=1);
        |""".stripMargin

    val env = "last_day=3"
    SQLUDS.run(spark, Array(s"file:$sql", s"env:$env"))
  }
}
