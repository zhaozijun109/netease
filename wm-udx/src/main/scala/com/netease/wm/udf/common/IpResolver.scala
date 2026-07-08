package com.netease.wm.udf.common

import java.io.IOException
import java.net.UnknownHostException

import com.fasterxml.jackson.databind.JsonNode
import com.maxmind.db.{NodeCache, Reader}
import com.twitter.util.LruMap
import org.apache.hadoop.fs.{FileContext, Path}

object IpResolver {
  val IPV4_REG = "((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])"
  val IPV6_REG = "(([0-9a-fA-F]{1,4}:){7,7}[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,7}:|([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|:((:[0-9a-fA-F]{1,4}){1,7}|:)|fe80:(:[0-9a-fA-F]{0,4}){0,4}%[0-9a-zA-Z]{1,}|::(ffff(:0{1,4}){0,1}:){0,1}((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])|([0-9a-fA-F]{1,4}:){1,4}:((25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9])\\.){3,3}(25[0-5]|(2[0-4]|1{0,1}[0-9]){0,1}[0-9]))"

  case class LruCache(capacity: Int) extends NodeCache {
    private val _cache = new LruMap[Int, JsonNode](capacity)
    override def get(key: Int, loader: NodeCache.Loader): JsonNode = {
      val k = key
      val value = _cache.get(k)
      if (value.isDefined) {
        value.get
      } else {
        val loaded = loader.load(key)
        _cache.put(k, loaded)
        loaded
      }
    }
  }
}

case class IpResolver(ipFilePath: String, cacheSize: Int = 4096) {
  import IpResolver._

  @transient var reader: Reader = null
  try
    reader = new Reader(FileContext.getFileContext.open(new Path(ipFilePath)), LruCache(cacheSize))
  catch {
    case e: IOException =>
      new RuntimeException("read geop ip file error", e)
  }

  /**
   * resolve ip to country, province and city
   *
   * @param ip
   * @return
   */
  def resolveIp(ip: String): Option[(String, String, String)] = {
    import java.io.IOException
    import java.net.InetAddress

    if(reader == null) reader = new Reader(FileContext.getFileContext.open(new Path(ipFilePath)), LruCache(cacheSize))

    if (ip != null && (ip.matches(IPV4_REG) || ip.matches(IPV6_REG))) {
      try {
        val address = InetAddress.getByName(ip)
        val response = reader.get(address)
        val country = response.get("country").get("names").get("zh_CN").textValue
        val province = response.get("subdivisions").get("names").get("zh_CN").textValue
        val city = response.get("city").get("names").get("zh_CN").textValue
        Some((country, province, city))
      } catch {
        case _: UnknownHostException =>
          println(s"unKnown host exception ip: $ip")
          Some(("异常","异常","异常"))
        case _: IOException =>
          None // ignored
        case _: NullPointerException =>
          println(s"can't resolve ip: $ip")
          None
      }
    } else {
      None
    }
  }
}
