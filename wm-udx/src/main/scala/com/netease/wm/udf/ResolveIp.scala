package com.netease.wm.udf

import java.util

import com.netease.wm.udf.common.IpResolver
import org.apache.hadoop.hive.ql.exec.Description
import org.apache.hadoop.hive.ql.metadata.HiveException
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory
import org.apache.hadoop.hive.serde2.objectinspector.{ObjectInspector, ObjectInspectorFactory}
import org.apache.hadoop.io.Text

@Description(name = "ResolveIp", value = "resolve ip to country province city")
class ResolveIp extends GenericUDF {
  lazy val ipResolver = new IpResolver("hdfs://gy-cluster8/user/da_lofter/ip/ip.mmdb")
  override def initialize(objectInspectors: Array[ObjectInspector]): ObjectInspector = {
    val structFieldNames = new util.ArrayList[String]
    val structFieldObjectInspectors = new util.ArrayList[ObjectInspector]

    structFieldNames.add("country")
    structFieldObjectInspectors.add(PrimitiveObjectInspectorFactory.writableStringObjectInspector)
    structFieldNames.add("province")
    structFieldObjectInspectors.add(PrimitiveObjectInspectorFactory.writableStringObjectInspector)
    structFieldNames.add("city")
    structFieldObjectInspectors.add(PrimitiveObjectInspectorFactory.writableStringObjectInspector)

    ObjectInspectorFactory.getStandardStructObjectInspector(structFieldNames, structFieldObjectInspectors)
  }

  override def evaluate(args: Array[GenericUDF.DeferredObject]): AnyRef = {
    if (args == null || args.length < 1) {
      throw new HiveException("args is empty");
    }

    val argObj = args(0).get();

    // get argument
    val ip: String = if (argObj.isInstanceOf[Text] ){
      argObj.asInstanceOf[Text].toString
    } else if (argObj.isInstanceOf[String]){
      argObj.asInstanceOf[String]
    } else if(argObj == null) {
      null
    } else {
      throw new HiveException("Argument is neither a Text nor String, it is a " + argObj.getClass().getCanonicalName());
    }

    val resolveResult = ipResolver.resolveIp(ip)
    val result = new Array[Object](3)
    result(0) = new Text(resolveResult.flatMap(s => Option(s._1)).getOrElse(""))
    result(1) = new Text(resolveResult.flatMap(s => Option(s._2)).getOrElse(""))
    result(2) = new Text(resolveResult.flatMap(s => Option(s._3)).getOrElse(""))
    result
  }

  override def getDisplayString(strings: Array[String]): String = "resolve_ip"
}
