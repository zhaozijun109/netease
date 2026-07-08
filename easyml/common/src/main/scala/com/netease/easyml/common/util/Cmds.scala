package com.netease.easyml.common.util

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializeFilter
import com.netease.easyml.common.cmds._
import org.apache.spark.internal.Logging
import org.apache.spark.ml.util.Identifiable
import org.apache.spark.sql.{DataFrame, SparkSession}

import java.util
import java.util.regex.Pattern
import scala.collection.JavaConverters._


/**
 * Created by linjiuning on 2022/8/12.
 */
object Cmds extends Logging {
  val REGISTER: Pattern = Pattern.compile("^create\\s+(?<overwrite>or\\s+replace\\s+)?temporary\\s+cmd\\s+(?<cmd>[a-zA-Z_]+)\\s+as\\s+(?<clazz>.+?);?$", Pattern.CASE_INSENSITIVE)
  val LET: Pattern = Pattern.compile("^let\\s+(?<key>[a-zA-Z_]+)\\s*=\\s*(?<sql>.+?);?$", Pattern.CASE_INSENSITIVE)
  val ENV: Pattern = Pattern.compile("^env\\s+(?<key>[a-zA-Z_]+)\\s*=\\s*(?<value>.+?);?$", Pattern.CASE_INSENSITIVE)
  val CMD: Pattern = Pattern.compile("^@\\s*(?<cmd>[a-zA-Z_]+)\\((?<options>.+?)\\);?$")
  val KV: Pattern = Pattern.compile("\\s*(?<key>[a-zA-Z_]+)\\s*=(?<value>.+)")

  val PARAM_LET: Pattern = Pattern.compile("^\\$\\((?<sql>.+?)\\);?$")
  val PARAM_CMD: Pattern = Pattern.compile("^\\$(?<cmd>[a-zA-Z_]+)\\((?<options>.+?)\\);?$")

  var STOP: Boolean = false
  var CMD_REGISTER: Map[String, UserDefinedCmd[_]] = Map()
  var VARS: Map[String, String] = Map()

  def register(name: String, cmd: UserDefinedCmd[_], overwrite: Boolean = false): Unit = {
    if (CMD_REGISTER.contains(name) && !overwrite) {
      throw new IllegalArgumentException(s"name=$name already exists.")
    }
    logInfo(s"register cmd: $name")
    CMD_REGISTER = CMD_REGISTER + (name -> cmd)
  }

  def run(spark: SparkSession, text: String): DataFrame = {
    var df: DataFrame = null
    if (!STOP) {
      if (Cmds.isRegisterCmd(text)) {
        println(s"EXECUTE SQL: $text")
        Cmds.register(text)
      } else if (Cmds.isEnvCmd(text)) {
        println(s"EXECUTE ENV: $text")
        Cmds.env(text)
      } else if (Cmds.isLetCmd(text)) {
        println(s"EXECUTE LET: $text")
        Cmds.let(spark, text)
      } else if (Cmds.isCmd(text)) {
        println(s"EXECUTE CMD: $text")
        df = Cmds.execute(spark, text)
      } else {
        val nText = Cmds.render(text)
        println(s"EXECUTE SQL: $nText")
        df = spark.sql(nText)
      }
    }
    df
  }

  def isRegisterCmd(text: String): Boolean = {
    val m = REGISTER.matcher(text)
    m.find()
  }

  def register(text: String): Unit = {
    val m = REGISTER.matcher(text)
    assert(m.find())
    val overwrite = if (m.groupCount() == 3) true else false
    val cmd = m.group("cmd")
    val clazz = normalize(m.group("clazz"))
    val udc = SparkUtil.classForName(clazz).newInstance().asInstanceOf[UserDefinedCmd[_]]
    register(cmd, udc, overwrite)
  }

  def isLetCmd(text: String): Boolean = {
    val m = LET.matcher(text)
    m.find()
  }

  def let(spark: SparkSession, text: String): Unit = {
    val m = LET.matcher(text)
    assert(m.find())
    val key = m.group("key")
    val sql = m.group("sql")
    val value = executeExpr(spark, sql)
    println(s"CMD: let, Args: $key=$value")
    VARS = VARS + (key -> value)
  }

  def paramLet(spark: SparkSession, text: String): String = {
    val m = PARAM_LET.matcher(text)
    assert(m.find())
    val sql = m.group("sql")
    executeExpr(spark, sql)
  }

  def isEnvCmd(text: String): Boolean = {
    val m = ENV.matcher(text)
    m.find()
  }

  def env(text: String): Unit = {
    val m = ENV.matcher(text)
    assert(m.find())
    val key = m.group("key")
    val value = normalize(m.group("value"))
    println(s"CMD: env, Args: $key=$value")
    VARS = VARS + (key -> value)
  }

  def isCmd(text: String): Boolean = {
    val m = CMD.matcher(text)
    m.find() && CMD_REGISTER.contains(m.group("cmd"))
  }

  def isParamLetCmd(text: String): Boolean = {
    val m = PARAM_LET.matcher(text)
    m.find()
  }

  def isParamCmd(text: String): Boolean = {
    val m = PARAM_CMD.matcher(text)
    m.find() && CMD_REGISTER.contains(m.group("cmd"))
  }

  def normalize(text: String, render: Boolean = true): String = {
    var nText = if ((text.startsWith("'") && text.endsWith("'")) ||
      (text.startsWith("\"") && text.endsWith("\""))) {
      text.substring(1, text.length - 1)
    } else {
      text
    }
    if (render) {
      nText = Cmds.render(nText)
    }
    nText
  }

  def render(text: String): String = {
    render(text, VARS)
  }

  def render(text: String, vars: Map[String, String]): String = {
    var nText = text
    if (vars.nonEmpty) {
      vars.foreach { case (k, v) => nText = nText.replaceAll("\\$\\{" + k + "}", v) }
    }
    nText
  }

  def execute(spark: SparkSession, text: String): DataFrame = {
    val m = CMD.matcher(text)
    if (m.find()) {
      val cmd = m.group("cmd")
      val options = m.group("options")
      val kvs = new util.HashMap[String, String]()
      StringUtil.safeSplit(options, ",", "\"'{", "\"'}").asScala
        .foreach(kv => {
          val m = KV.matcher(kv)
          assert(m.find(), kv)
          val key = m.group("key")
          var value = normalize(m.group("value").trim)
          if (Cmds.isParamCmd(value)) {
            println(s"EXECUTE PARAM CMD: $value")
            val tmpTable = Identifiable.randomUID("tmp")
            value = value.replaceFirst("\\$", "@")
            Cmds.execute(spark, value).createOrReplaceTempView(tmpTable)
            value = tmpTable
          } else if (Cmds.isParamLetCmd(value)) {
            println(s"EXECUTE PARAM LET: $value")
            value = Cmds.paramLet(spark, value)
          }
          kvs.put(key, value)
        })
      println(s"CMD: $cmd, Args: ${JSON.toJSONString(kvs, new Array[SerializeFilter](0))}")
      val viewName = kvs.remove("as")
      val df = CMD_REGISTER(cmd)(spark, kvs)
      if (viewName != null) {
        df.createOrReplaceTempView(viewName)
      }
      df
    } else null
  }

  def executeExpr(spark: SparkSession, expr: String): String = {
    var sql = normalize(expr)
    val df = if (isCmd(sql)) {
      execute(spark, sql)
    } else {
      if (!sql.toLowerCase.startsWith("select ")) {
        sql = "select " + sql
      }
      spark.sql(sql)
    }
    df.rdd.map(row => row.get(0).toString).first()
  }

  def setStop(stop: Boolean): Boolean = {
    val ret = STOP
    STOP = stop
    ret
  }

  // basic
  register("sql", new Sql)
  register("load", new Load)
  register("save", new Save)
  register("show", new Show)
  register("count", new Count)
  register("cache", new Cache)
  register("persist", new Persist)
  register("unpersist", new Unpersist)
  register("repartition", new Repartition)
  register("coalesce", new Coalesce)

  // ml
  register("fit", new Fit)
  register("transform", new Transform)
  register("fit_transform", new FitTransform)
  register("uds", new Uds)

  // io
  register("rm", new Rm)
  register("mkdir", new Mkdir)
  register("touch", new Touch)
  register("echo", new Echo)
  register("cp", new Cp)
  register("mv", new Mv)
  register("print", new Print)
  register("load_wordvec", new LoadWordVec)
  register("load_ann_json", new LoadAnnJson)

  // control
  register("if", new If)
  register("for", new For)
  register("until", new Until)
  register("exit", new Exit)
  register("sleep", new Sleep)
}
