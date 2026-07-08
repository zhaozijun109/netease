package com.netease.bdms.ndi.service.web;

import javax.crypto.spec.PSource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.util.RedisUtil;
import com.netease.bdms.ndi.service.web.util.ResponseResult;

import java.net.URLDecoder;
import java.net.URLEncoder;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.spark.sql.catalyst.TableIdentifier;
import org.apache.spark.sql.execution.SparkSqlParser;
import org.apache.spark.sql.execution.datasources.CreateTable;
import org.apache.spark.sql.internal.SQLConf;
import org.apache.spark.sql.types.StructField;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import scala.collection.JavaConverters;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName CommonTests
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class CommonTests extends DataIntegrationWebTests {
  private static final Logger log = LoggerFactory.getLogger(CommonTests.class);
  @Autowired
  private RestTemplate restTemplate;

  @Autowired
  private RedisUtil redisUtil;


  @Test
  public void testExecutorService() {

  }


  @Test
  public void testRestTemplateGET() {

  }

  @Test
  public void testRestTemplatePOST() {

  }

  public ResponseResult postForRequestBody(String url, HttpHeaders httpHeaders, Object param, Type resultType) {
    HttpEntity<Object> httpEntity = new HttpEntity<>(param, httpHeaders);
    JSONObject jsonObject = restTemplate.postForObject(url, httpEntity, JSONObject.class);
    ResponseResult responseResult = JSONObject.parseObject(jsonObject.toString(), resultType);
    return responseResult;
  }

  @Test
  public void testRedis() {
    Long result = redisUtil.hset("a", "a", "a");
    System.out.println(result);
    Long result1 = redisUtil.hsetWithExpire("a", "a", "a", 1000);
    System.out.println(result1);

    String value = redisUtil.hget("a", "b");
    System.out.println(value);
  }

  @Test
  public void testJSONObject() {

  }


  @Test
  public void testJson() {

  }

  @Test
  public void testException() {
    JSONObject jsonObject = JSON.parseObject("");
    JSONObject.parseObject("");
    String message = null;
    try {
      System.out.println(message.getBytes());
    } catch (Exception e) {
      e.printStackTrace();

      log.error("===========");
      log.error("e.getLocalizedMessage()", e.getLocalizedMessage());
      log.error("===========");
      log.error("e.getMessage()", e.getMessage());
      log.error("===========");
      log.error("e.getCause()", e.getCause());
      log.error("===========");
//            log.error("e.getStackTrace()", e.getStackTrace());
      log.error("===========");
//            log.error("e.getSuppressed()", e.getSuppressed());



    }
  }

  @Test
  public void testSQLParse() {
    try {
      SparkSqlParser parser = new SparkSqlParser();
      String sql = "CREATE TABLE `azkaban_autotest_db.v542_impala_sync`(\n" +
          "  `id` string)\n" +
          "PARTITIONED BY ( \n" +
          "  `date` string, " +
          "  `hour` int)\n" +
          "ROW FORMAT DELIMITED \n" +
          "  FIELDS TERMINATED BY '\\u0001' \n" +
          "STORED AS INPUTFORMAT \n" +
          "  'org.apache.hadoop.mapred.TextInputFormat' \n" +
          "OUTPUTFORMAT \n" +
          "  'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'\n" +
          "LOCATION\n" +
          "  'hdfs://hz-cluster3/warehouse/mammut/azkaban_autotest_db.db/v542_impala_sync'\n" +
          "TBLPROPERTIES (\n" +
          "  'SYNC_METASTORE'='OFF', \n" +
          "  'last_modified_by'='mammut', \n" +
          "  'last_modified_time'='1553166629', \n" +
          "  'table.creator'='hzshanlinna@corp.netease.com', \n" +
          "  'table.source'='\\u81EA\\u5B9A\\u4E49', \n" +
          "  'transient_lastDdlTime'='1553166629')";
      CreateTable plan = (CreateTable) parser.parsePlan(sql);
      Map<String, Object> result = new HashMap<>();

      if (plan.tableDesc().partitionSchema() != null) {
        List<Map<String, String>> partitions = new ArrayList<>();
        result.put("partitions", partitions);
        List<StructField> fields =
            JavaConverters.seqAsJavaListConverter(plan.tableDesc().partitionSchema().toList()).asJava();
        for (StructField field : fields) {
          Map<String, String> filedAttribute = new HashMap<>();
          partitions.add(filedAttribute);
          filedAttribute.put("datatype", field.dataType().typeName());
          filedAttribute.put("name", field.name());
        }
      } else {
        result.put("partitions", new ArrayList<Map<String, String>>());
      }
      TableIdentifier identifier = plan.tableDesc().identifier();
      String table = identifier.table();
      String[] splits = table.split("\\.", 2);
      if (splits.length == 1) {
        result.put("table", splits[0]);
      } else {
        result.put("database", splits[0]);
        result.put("table", splits[1]);
      }
      List<Map<String, String>> columns = new ArrayList<>();
      result.put("columns", columns);
      List<StructField> fields = JavaConverters.seqAsJavaListConverter(plan.tableDesc().dataSchema().toList()).asJava();
      for (StructField field : fields) {
        Map<String, String> filedAttribute = new HashMap<>();
        columns.add(filedAttribute);
        filedAttribute.put("datatype", field.dataType().typeName());
        filedAttribute.put("name", field.name());
      }
      System.out.println(result);
    } catch (Exception e) {
      System.out.println("Failed to parse");
    }
  }

  public static void main(String[] args) {
    List<String> stringList = Lists.newArrayList();
    stringList.add("mammut_qa");
    if (stringList.contains("mammut_qa")) {
      System.out.println("true");
    }
  }
}
