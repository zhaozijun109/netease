package com.netease.yuanqi.metaservice

import com.fasterxml.jackson.databind.{DeserializationFeature, JsonNode, ObjectMapper}
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.netease.yuanqi.config.MammutConfig
import okhttp3.{HttpUrl, MediaType, OkHttpClient, Request, RequestBody}

import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import scala.collection.JavaConverters._
import scala.collection.mutable

/**
 * Jackson-based JSON helper — provides convenient extension methods on JsonNode.
 */
object JsonHelper {
  /** Shared ObjectMapper instance with Scala module registered. */
  private val mapper: ObjectMapper = new ObjectMapper()
    .registerModule(DefaultScalaModule)
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  /** Parse a JSON string into a JsonNode tree. */
  def parse(json: String): JsonNode = mapper.readTree(json)

  /** Serialize any value (JsonNode, Map, case class, etc.) to a JSON string. */
  def toJson(value: Any): String = mapper.writeValueAsString(value)

  /** Create a new empty ObjectNode. */
  def newObject(): ObjectNode = mapper.createObjectNode()

  /**
   * Implicit class adding safe accessor methods to JsonNode,
   * providing a similar API surface to the old JsonObject helpers.
   */
  implicit class RichJsonNode(val node: JsonNode) extends AnyVal {

    def getString(key: String): Option[String] = {
      val v = node.get(key)
      if (v != null && v.isTextual) Some(v.asText()) else None
    }

    def getInt(key: String): Option[Int] = {
      val v = node.get(key)
      if (v != null && v.isNumber) Some(v.asInt()) else None
    }

    def getLong(key: String): Option[Long] = {
      val v = node.get(key)
      if (v != null && v.isNumber) Some(v.asLong()) else None
    }

    def getBool(key: String): Option[Boolean] = {
      val v = node.get(key)
      if (v != null && v.isBoolean) Some(v.asBoolean()) else None
    }

    def getArray(key: String): Option[List[JsonNode]] = {
      val v = node.get(key)
      if (v != null && v.isArray) Some(v.asScala.toList) else None
    }

    def getObject(key: String): Option[JsonNode] = {
      val v = node.get(key)
      if (v != null && v.isObject) Some(v) else None
    }
  }
}

// ─── Domain Models ──────────────────────────────────────────────────────────────

/**
 * Datasource info returned by GET /datasource/v1/list (easymetahub).
 * Matches actual response: datasourceId, creator, createTime, groupId, product,
 * datasourceName, datasourceType, info{}, catalogName, updateTime
 */
case class DatasourceInfo(
  datasourceId: Int,
  creator: Option[String] = None,
  createTime: Option[Long] = None,
  groupId: Option[Int] = None,
  product: Option[String] = None,
  datasourceName: Option[String] = None,
  datasourceType: Option[String] = None,
  info: Map[String, String] = Map.empty,
  catalogName: Option[String] = None,
  updateTime: Option[Long] = None,
  rawJson: Option[JsonNode] = None
)

/**
 * Column/field info embedded in GET /table/v1/list response.
 * Actual fields: fieldName, fieldType, partitionKey (Boolean), comment
 */
case class TableFieldList(
  fieldName: Option[String] = None,
  fieldType: Option[String] = None,
  partitionKey: Option[Boolean] = None,
  comment: Option[String] = None
)

/**
 * Table list item returned by GET /table/v1/list (easymetahub).
 * Actual response fields: table, owner, creator, createdTime (String),
 * comment, tableType, fields[{fieldName, fieldType, partitionKey, comment}]
 */
case class TableListItem(
  table: Option[String] = None,
  owner: Option[String] = None,
  creator: Option[String] = None,
  createdTime: Option[String] = None,
  comment: Option[String] = None,
  tableType: Option[String] = None,
  fields: List[TableFieldList] = Nil,
  rawJson: Option[JsonNode] = None
)

/** Owner nested object in POST /table/v1/info/get response. */
case class OwnerInfo(
  user: Option[String] = None,
  fullName: Option[String] = None
)

/** Business info nested in POST /table/v1/info/get response. */
case class BusinessInfo(
  impalaSync: Option[Boolean] = None,
  recommend: Option[Boolean] = None,
  core: Option[Boolean] = None,
  hasCollected: Option[Boolean] = None,
  hasSubscribed: Option[Boolean] = None,
  rawJson: Option[JsonNode] = None
)

/** Storage info nested in POST /table/v1/info/get response. */
case class StorageInfo(
  totalSize: Option[String] = None,
  filesNumber: Option[Int] = None,
  storageType: Option[String] = None,
  serializationType: Option[String] = None,
  location: Option[String] = None,
  createTime: Option[String] = None,
  updateTime: Option[String] = None,
  partition: Option[Boolean] = None,
  partitionLifeCycle: Option[String] = None,
  tableLifeCycle: Option[String] = None,
  deleteDir: Option[Boolean] = None,
  rawJson: Option[JsonNode] = None
)

/**
 * Table detail returned by POST /table/v1/info/get (easydmap).
 * Actual response: groupId, groupName, productId, product, clusterId, clusterName,
 * db, table, owner{user, fullName}, primaryKeys[], datasourceName, datasourceType,
 * tableType, referCount, readCount, businessInfo{}, storageInfo{}, datasourceId, etc.
 */
case class TableInfoV1(
  groupId: Option[Int] = None,
  groupName: Option[String] = None,
  productId: Option[Int] = None,
  product: Option[String] = None,
  clusterId: Option[String] = None,
  clusterName: Option[String] = None,
  db: Option[String] = None,
  table: Option[String] = None,
  owner: Option[OwnerInfo] = None,
  primaryKeys: List[String] = Nil,
  datasourceName: Option[String] = None,
  datasourceType: Option[String] = None,
  tableType: Option[String] = None,
  referCount: Option[Int] = None,
  readCount: Option[Int] = None,
  businessInfo: Option[BusinessInfo] = None,
  storageInfo: Option[StorageInfo] = None,
  datasourceId: Option[Int] = None,
  rawJson: Option[JsonNode] = None
)

/**
 * Column info inside tableMetaInfo from POST /table/v3/info/get.
 * Actual fields: columnName, columnType, primaryKey, description, unique
 */
case class ColumnInfo(
  columnName: Option[String] = None,
  columnType: Option[String] = None,
  primaryKey: Option[Boolean] = None,
  description: Option[String] = None,
  unique: Option[Boolean] = None
)

/**
 * tableMetaInfo nested object from POST /table/v3/info/get.
 * Contains storage, column, partition, and lifecycle information.
 */
case class TableMetaInfo(
  description: Option[String] = None,
  storagePath: Option[String] = None,
  serializationFormat: Option[String] = None,
  columnInfos: List[ColumnInfo] = Nil,
  owner: Option[String] = None,
  ownerPrincipal: Option[String] = None,
  partTable: Option[Boolean] = None,
  tableLifeCycle: Option[String] = None,
  partitionLifeCycle: Option[String] = None,
  lastModifiedTime: Option[String] = None,
  tableType: Option[String] = None,
  tableCategory: Option[String] = None,
  serializationType: Option[String] = None,
  totalSize: Option[String] = None,
  compressionType: Option[String] = None,
  lzoCompression: Option[Boolean] = None,
  filesNumber: Option[Int] = None,
  impalaSync: Option[Boolean] = None,
  exist: Option[Boolean] = None,
  rawJson: Option[JsonNode] = None
)

/**
 * Table meta-info V3 returned by POST /table/v3/info/get (easydmap).
 * Top-level: groupId, product, clusterId, accountId, catalog, db, table,
 * datasourceType, tableMetaInfo{...}
 */
case class TableInfoV3(
  groupId: Option[Int] = None,
  product: Option[String] = None,
  clusterId: Option[String] = None,
  accountId: Option[Int] = None,
  catalog: Option[String] = None,
  db: Option[String] = None,
  table: Option[String] = None,
  datasourceType: Option[String] = None,
  tableMetaInfo: Option[TableMetaInfo] = None,
  rawJson: Option[JsonNode] = None
)

/**
 * Field info returned by POST /field/v1/info/get (easydmap).
 * result is a direct array of: id, name, type, comment, enumKey, partitionKey, primaryKey, logicProperties[]
 */
case class FieldInfo(
  id: Option[Int] = None,
  name: Option[String] = None,
  fieldType: Option[String] = None,
  comment: Option[String] = None,
  enumKey: Option[Boolean] = None,
  partitionKey: Option[Boolean] = None,
  primaryKey: Option[Boolean] = None,
  rawJson: Option[JsonNode] = None
)

/**
 * Partition info returned by GET /table/v1/partition/list (easymetahub).
 * Actual fields: catalog, db, table, partition, createTime (String), updateTime (String),
 * metadata{totalSize, numRows, rawDataSize, numFiles, ...}, serde{uri, ...}
 */
case class PartitionInfo(
  catalog: Option[String] = None,
  db: Option[String] = None,
  table: Option[String] = None,
  partition: Option[String] = None,
  createTime: Option[String] = None,
  updateTime: Option[String] = None,
  totalSize: Option[Long] = None,
  numFiles: Option[Long] = None,
  numRows: Option[Long] = None,
  location: Option[String] = None,
  rawJson: Option[JsonNode] = None
)

// ─── Service ────────────────────────────────────────────────────────────────────

class MammutMetaService extends AutoCloseable {
  private val httpClient = new OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()

  /** Default page size for paginated requests. */
  private val DEFAULT_PAGE_SIZE = 100

  // ─── Signature Management ───────────────────────────────────────────────────

  /**
   * Generate a fresh API signature from MD5(secretKey + timestamp).
   * Called on every request to ensure the timestamp is always within the
   * server's valid window (-5min, +5min).
   *
   * @return (sig, timestamp) pair to be included in the request
   */
  private def generateSignature(): (String, Long) = {
    val timestamp = System.currentTimeMillis()
    val value = MammutConfig.secretKey + timestamp
    val sig = MessageDigest.getInstance("MD5")
      .digest(value.getBytes("UTF-8"))
      .map("%02x".format(_)).mkString
    (sig, timestamp)
  }

  /**
   * Get a freshly generated sig (for testing/debugging).
   * @return string
   */
  def getSig: String = generateSignature()._1

  // ─── HTTP Helpers ───────────────────────────────────────────────────────────

  /**
   * Build the base URL for a given service path.
   * e.g. baseUrl/serviceName/path
   */
  private def buildBaseUrl(serviceName: String, path: String): String = {
    val base = MammutConfig.baseUrl
    s"$base/$serviceName$path"
  }

  private val JSON_MEDIA_TYPE: MediaType = MediaType.parse("application/json; charset=utf-8")

  /**
   * Append common authentication query params to an HttpUrl.Builder.
   * Generates a fresh signature for every request to avoid timestamp expiry.
   * Used for GET requests — all parameters go into the URL query string.
   */
  private def appendAuthParams(urlBuilder: HttpUrl.Builder): HttpUrl.Builder = {
    val (sig, timestamp) = generateSignature()
    urlBuilder
      .addQueryParameter("product", MammutConfig.product)
      .addQueryParameter("user", MammutConfig.user)
      .addQueryParameter("accessKey", MammutConfig.accessKey)
      .addQueryParameter("sig", sig)
      .addQueryParameter("timestamp", timestamp.toString)
      .addQueryParameter("authType", MammutConfig.authType)
  }

  /**
   * Build common authentication fields as an ObjectNode for inclusion in POST JSON body.
   * Generates a fresh signature for every request to avoid timestamp expiry.
   * Used for POST requests — all parameters go into the Request Body as JSON.
   */
  private def buildAuthBody(): ObjectNode = {
    val (sig, timestamp) = generateSignature()
    val node = JsonHelper.newObject()
    node.put("product", MammutConfig.product)
    node.put("user", MammutConfig.user)
    node.put("accessKey", MammutConfig.accessKey)
    node.put("sig", sig)
    node.put("timestamp", timestamp)
    node.put("authType", MammutConfig.authType)
    node
  }

  /**
   * Parse the response body and check for API-level errors.
   * Shared by both executeGet and executePost.
   */
  private def parseResponse(response: okhttp3.Response): JsonNode = {
    import JsonHelper.RichJsonNode
    try {
      val body = response.body().string()
      if (!response.isSuccessful) {
        throw new RuntimeException(s"HTTP request failed [${response.code()}]: $body")
      }
      val root = JsonHelper.parse(body)
      if (!root.isObject) {
        throw new RuntimeException(s"Unexpected response type: ${root.getNodeType}")
      }
      // Check API-level success (common pattern: code == 0 or code == 200)
      root.getInt("code") match {
        case Some(code) if code != 0 && code != 200 =>
          val msg = root.getString("message").orElse(root.getString("msg")).getOrElse("unknown error")
          throw new RuntimeException(s"API error [code=$code]: $msg")
        case _ => root
      }
    } finally {
      response.close()
    }
  }

  /**
   * Execute a GET request and return the parsed response body as JsonNode.
   * All request parameters are placed in the URL query string.
   * Throws RuntimeException on HTTP or API-level errors.
   */
  private def executeGet(url: HttpUrl): JsonNode = {
    val request = new Request.Builder().url(url).get().build()
    val response = httpClient.newCall(request).execute()
    parseResponse(response)
  }

  /**
   * Execute a POST request with JSON body and return the parsed response body as JsonNode.
   * All request parameters (including auth) are placed in the JSON Request Body.
   * Content-Type is set to application/json.
   * Throws RuntimeException on HTTP or API-level errors.
   *
   * @param url      the target URL (without query params for POST)
   * @param jsonBody the ObjectNode to send as request body
   */
  private def executePost(url: HttpUrl, jsonBody: ObjectNode): JsonNode = {
    val jsonString = JsonHelper.toJson(jsonBody)
    val requestBody = RequestBody.create(JSON_MEDIA_TYPE, jsonString)
    val request = new Request.Builder()
      .url(url)
      .post(requestBody)
      .header("Content-Type", "application/json")
      .build()
    val response = httpClient.newCall(request).execute()
    parseResponse(response)
  }

  // ─── Public API ─────────────────────────────────────────────────────────────

  /**
   * Fetch all pages of a GET list API that uses pageNum/pageSize pagination
   * and wraps results in { "result": { "list": [...], "totalCount": N } }.
   *
   * This variant handles APIs like /datasource/v1/list whose pagination param
   * is "pageNum" (instead of "pageNo") and whose response is wrapped under
   * the "result" key (instead of "data").
   *
   * @param rawUrl      the base URL (without page params)
   * @param extraParams additional query parameters beyond auth
   * @param pageSize    number of items per page
   * @return all collected JsonNode items across all pages
   */
  private def fetchAllPagesGet(rawUrl: String,
                                 extraParams: Map[String, String] = Map.empty,
                                 pageSize: Int = DEFAULT_PAGE_SIZE): List[JsonNode] = {
    import JsonHelper.RichJsonNode
    val allItems = mutable.ListBuffer.empty[JsonNode]
    var pageNum = 1
    var hasMore = true

    while (hasMore) {
      val urlBuilder = HttpUrl.parse(rawUrl).newBuilder()
      appendAuthParams(urlBuilder)
      extraParams.foreach { case (k, v) => urlBuilder.addQueryParameter(k, v) }
      urlBuilder.addQueryParameter("pageNum", pageNum.toString)
      urlBuilder.addQueryParameter("pageSize", pageSize.toString)

      val root = executeGet(urlBuilder.build())

      // Extract from { "result": { "list": [...], "totalCount": N } }
      val resultObj = root.getObject("result")
      val items = resultObj.flatMap(_.getArray("list")).getOrElse(List.empty)
      val totalCount = resultObj.flatMap(_.getInt("totalCount"))

      allItems ++= items

      totalCount match {
        case Some(t) => hasMore = allItems.size < t
        case None    => hasMore = items.size >= pageSize
      }

      pageNum += 1
    }

    allItems.toList
  }

  // ─── Datasource API ────────────────────────────────────────────────────────

  /**
   * Get the list of datasources via GET /datasource/v1/list.
   * Automatically paginates to collect all results.
   *
   * @param datasourceId   optional filter by datasource id
   * @param datasourceType optional filter by datasource type (e.g. "hive")
   * @param product        optional filter by product (overrides config default if specified)
   * @return list of DatasourceInfo
   */
  def getDatasourceList(datasourceId: Option[Int] = None,
                        datasourceType: Option[String] = None,
                        product: Option[String] = None): List[DatasourceInfo] = {
    import JsonHelper.RichJsonNode
    val rawUrl = buildBaseUrl(MammutConfig.metaServiceName, "/datasource/v1/list")

    val extraParams = mutable.Map.empty[String, String]
    datasourceId.foreach(id => extraParams("datasourceId") = id.toString)
    datasourceType.foreach(t => extraParams("datasourceType") = t)
    product.foreach(p => extraParams("product") = p)

    val allItems = fetchAllPagesGet(rawUrl, extraParams.toMap)

    allItems.filter(_.isObject).map { obj =>
      // Parse the nested "info" object into a flat Map[String, String]
      val infoMap: Map[String, String] = obj.getObject("info")
        .map { infoNode =>
          infoNode.fields().asScala.map { entry =>
            entry.getKey -> entry.getValue.asText()
          }.toMap
        }
        .getOrElse(Map.empty)

      DatasourceInfo(
        datasourceId = obj.getInt("datasourceId").getOrElse(0),
        creator = obj.getString("creator"),
        createTime = obj.getLong("createTime"),
        groupId = obj.getInt("groupId"),
        product = obj.getString("product"),
        datasourceName = obj.getString("datasourceName"),
        datasourceType = obj.getString("datasourceType"),
        info = infoMap,
        catalogName = obj.getString("catalogName"),
        updateTime = obj.getLong("updateTime"),
        rawJson = Some(obj)
      )
    }
  }

  // ─── Table List API (easymetahub) ──────────────────────────────────────────

  /**
   * Get the list of tables via GET /table/v1/list (easymetahub).
   * Automatically paginates to collect all results.
   *
   * Actual response per item: table, owner, creator, createdTime (String),
   * comment, tableType, fields[{fieldName, fieldType, partitionKey, comment}]
   *
   * @param db           database name
   * @param datasourceId optional datasource id (defaults to config)
   * @return list of TableListItem
   */
  def getTableList(db: String,
                   datasourceId: Option[Int] = None): List[TableListItem] = {
    import JsonHelper.RichJsonNode
    val rawUrl = buildBaseUrl(MammutConfig.metaServiceName, "/table/v1/list")

    val extraParams = mutable.Map[String, String](
      "datasourceId" -> datasourceId.getOrElse(MammutConfig.datasourceId).toString,
      "db" -> db
    )

    val allItems = fetchAllPagesGet(rawUrl, extraParams.toMap)

    allItems.filter(_.isObject).map { obj =>
      // Parse embedded fields array
      val fields = obj.getArray("fields")
        .getOrElse(List.empty)
        .filter(_.isObject)
        .map { fObj =>
          TableFieldList(
            fieldName = fObj.getString("fieldName"),
            fieldType = fObj.getString("fieldType"),
            partitionKey = fObj.getBool("partitionKey"),
            comment = fObj.getString("comment")
          )
        }

      TableListItem(
        table = obj.getString("table"),
        owner = obj.getString("owner"),
        creator = obj.getString("creator"),
        createdTime = obj.getString("createdTime"),
        comment = obj.getString("comment"),
        tableType = obj.getString("tableType"),
        fields = fields,
        rawJson = Some(obj)
      )
    }
  }

  // ─── Table Detail API (easydmap) ───────────────────────────────────────────

  /**
   * Get table detail via POST /table/v1/info/get (easydmap).
   *
   * @param db           database name
   * @param table        table name
   * @param datasourceId optional datasource id (defaults to config)
   * @return TableDetail
   */
  def getTableDetail(db: String,
                     table: String,
                     datasourceId: Option[Int] = None): TableInfoV1 = {
    import JsonHelper.RichJsonNode
    val rawUrl = buildBaseUrl(MammutConfig.mapServiceName, "/table/v1/info/get")
    val url = HttpUrl.parse(rawUrl)

    val body = buildAuthBody()
    body.put("datasourceId", datasourceId.getOrElse(MammutConfig.datasourceId))
    body.put("db", db)
    body.put("table", table)

    val root = executePost(url, body)
    val result = root.getObject("result").getOrElse(root)

    // Parse owner nested object: {"user":"...", "fullName":"..."}
    val ownerInfo: Option[OwnerInfo] = result.getObject("owner").map { ownerObj =>
      OwnerInfo(
        user = ownerObj.getString("user"),
        fullName = ownerObj.getString("fullName")
      )
    }

    // Parse storageInfo nested object
    val storageInfoObj: Option[StorageInfo] = result.getObject("storageInfo").map { si =>
      StorageInfo(
        totalSize = si.getString("totalSize"),
        filesNumber = si.getInt("filesNumber"),
        storageType = si.getString("storageType"),
        serializationType = si.getString("serializationType"),
        location = si.getString("location"),
        createTime = si.getString("createTime"),
        updateTime = si.getString("updateTime"),
        partition = si.getBool("partition"),
        partitionLifeCycle = si.getString("partitionLifeCycle"),
        tableLifeCycle = si.getString("tableLifeCycle"),
        deleteDir = si.getBool("deleteDir"),
        rawJson = Some(si)
      )
    }

    // Parse businessInfo nested object
    val businessInfoObj: Option[BusinessInfo] = result.getObject("businessInfo").map { bi =>
      BusinessInfo(
        impalaSync = bi.getBool("impalaSync"),
        recommend = bi.getBool("recommend"),
        core = bi.getBool("core"),
        hasCollected = bi.getBool("hasCollected"),
        hasSubscribed = bi.getBool("hasSubscribed"),
        rawJson = Some(bi)
      )
    }

    // Parse primaryKeys array (e.g. ["-"])
    val primaryKeys = result.getArray("primaryKeys")
      .map(_.flatMap(n => if (n.isTextual) Some(n.asText()) else None))
      .getOrElse(Nil)

    TableInfoV1(
      groupId = result.getInt("groupId"),
      groupName = result.getString("groupName"),
      productId = result.getInt("productId"),
      product = result.getString("product"),
      clusterId = result.getString("clusterId"),
      clusterName = result.getString("clusterName"),
      db = result.getString("db"),
      table = result.getString("table"),
      owner = ownerInfo,
      primaryKeys = primaryKeys,
      datasourceName = result.getString("datasourceName"),
      datasourceType = result.getString("datasourceType"),
      tableType = result.getString("tableType"),
      referCount = result.getInt("referCount"),
      readCount = result.getInt("readCount"),
      businessInfo = businessInfoObj,
      storageInfo = storageInfoObj,
      datasourceId = result.getInt("datasourceId"),
      rawJson = Some(result)
    )
  }

  // ─── Table Detail V3 API (easydmap) ────────────────────────────────────────

  /**
   * Get table meta info V3 via POST /table/v3/info/get (easydmap).
   *
   * @param db             database name
   * @param table          table name
   * @param datasourceType datasource type (e.g. "hive")
   * @param catalog        optional catalog (defaults to config)
   * @param optionalInfos  optional list of additional info types to include
   * @return MetaInfoDto
   */
  def getTableDetailV3(db: String,
                       table: String,
                       datasourceType: String,
                       catalog: Option[String] = None,
                       optionalInfos: List[String] = Nil): TableInfoV3 = {
    import JsonHelper.RichJsonNode
    val rawUrl = buildBaseUrl(MammutConfig.mapServiceName, "/table/v3/info/get")
    val url = HttpUrl.parse(rawUrl)

    val body = buildAuthBody()
    body.put("catalog", catalog.getOrElse(MammutConfig.catalog))
    body.put("db", db)
    body.put("table", table)
    body.put("datasourceType", datasourceType)

    // optionalInfos is mandatory and must contain at least one valid value.
    // Valid values: tableMetaInfo, tableTagInfo, logicTableInfo, otherProductInfo, all
    // Default to ["tableMetaInfo"] when caller does not specify.
    val effectiveInfos = if (optionalInfos.nonEmpty) optionalInfos else List("tableMetaInfo")
    val arr = body.putArray("optionalInfos")
    effectiveInfos.foreach(arr.add)

    val root = executePost(url, body)
    val result = root.getObject("result").getOrElse(root)

    // Parse nested tableMetaInfo object
    val tableMetaInfoObj: Option[TableMetaInfo] = result.getObject("tableMetaInfo").map { tmi =>
      // Parse columnInfos array inside tableMetaInfo
      val columnInfos = tmi.getArray("columnInfos")
        .getOrElse(List.empty)
        .filter(_.isObject)
        .map { col =>
          ColumnInfo(
            columnName = col.getString("columnName"),
            columnType = col.getString("columnType"),
            primaryKey = col.getBool("primaryKey"),
            description = col.getString("description"),
            unique = col.getBool("unique")
          )
        }

      TableMetaInfo(
        description = tmi.getString("description"),
        storagePath = tmi.getString("storagePath"),
        serializationFormat = tmi.getString("serializationFormat"),
        columnInfos = columnInfos,
        owner = tmi.getString("owner"),
        ownerPrincipal = tmi.getString("ownerPrincipal"),
        partTable = tmi.getBool("partTable"),
        tableLifeCycle = tmi.getString("tableLifeCycle"),
        partitionLifeCycle = tmi.getString("partitionLifeCycle"),
        lastModifiedTime = tmi.getString("lastModifiedTime"),
        tableType = tmi.getString("tableType"),
        tableCategory = tmi.getString("tableCategory"),
        serializationType = tmi.getString("serializationType"),
        totalSize = tmi.getString("totalSize"),
        compressionType = tmi.getString("compressionType"),
        lzoCompression = tmi.getBool("lzoCompression"),
        filesNumber = tmi.getInt("filesNumber"),
        impalaSync = tmi.getBool("impalaSync"),
        exist = tmi.getBool("exist"),
        rawJson = Some(tmi)
      )
    }

    TableInfoV3(
      groupId = result.getInt("groupId"),
      product = result.getString("product"),
      clusterId = result.getString("clusterId"),
      accountId = result.getInt("accountId"),
      catalog = result.getString("catalog"),
      db = result.getString("db"),
      table = result.getString("table"),
      datasourceType = result.getString("datasourceType"),
      tableMetaInfo = tableMetaInfoObj,
      rawJson = Some(result)
    )
  }

  // ─── Field Detail API (easydmap) ───────────────────────────────────────────

  /**
   * Get field (column) details for a table via POST /field/v1/info/get (easydmap).
   * The API returns result as a direct array: { "result": [{id, name, type, comment, ...}, ...] }
   *
   * @param db           database name
   * @param table        table name
   * @param datasourceId optional datasource id (defaults to config)
   * @param clusterId    optional cluster id (defaults to config)
   * @return list of FieldDetail
   */
  def getFieldInfo(db: String,
                   table: String,
                   datasourceId: Option[Int] = None,
                   clusterId: Option[String] = None): List[FieldInfo] = {
    import JsonHelper.RichJsonNode
    val rawUrl = buildBaseUrl(MammutConfig.mapServiceName, "/field/v1/info/get")
    val url = HttpUrl.parse(rawUrl)

    val body = buildAuthBody()
    body.put("datasourceId", datasourceId.getOrElse(MammutConfig.datasourceId))
    body.put("db", db)
    body.put("table", table)
    body.put("clusterId", clusterId.getOrElse(MammutConfig.clusterId))

    val root = executePost(url, body)

    // result is an object: { "result": { "fields": [...], "primaryKeys": [...], "total": N } }
    val resultObj = root.getObject("result").getOrElse(root)
    val items: List[JsonNode] = resultObj.getArray("fields").getOrElse(List.empty)

    items.filter(_.isObject).map { obj =>
      FieldInfo(
        id = obj.getInt("id"),
        name = obj.getString("name"),
        fieldType = obj.getString("type"),
        comment = obj.getString("comment"),
        enumKey = obj.getBool("enumKey"),
        partitionKey = obj.getBool("partitionKey"),
        primaryKey = obj.getBool("primaryKey"),
        rawJson = Some(obj)
      )
    }
  }

  // ─── Partition Count API (easymetahub) ─────────────────────────────────────

  /**
   * Get partition count for a table via GET /table/v1/partition/count (easymetahub).
   *
   * @param db        database name
   * @param table     table name
   * @param catalog   optional catalog (defaults to config)
   * @param partition optional partition filter (e.g. "dt=20250101")
   * @return partition count
   */
  def getPartitionCount(db: String,
                        table: String,
                        catalog: Option[String] = None,
                        partition: Option[String] = None): Long = {
    import JsonHelper.RichJsonNode
    val rawUrl = buildBaseUrl(MammutConfig.metaServiceName, "/table/v1/partition/count")

    val urlBuilder = HttpUrl.parse(rawUrl).newBuilder()
    appendAuthParams(urlBuilder)
    urlBuilder.addQueryParameter("catalog", catalog.getOrElse(MammutConfig.catalog))
    urlBuilder.addQueryParameter("db", db)
    urlBuilder.addQueryParameter("table", table)
    partition.foreach(p => urlBuilder.addQueryParameter("partition", p))

    val root = executeGet(urlBuilder.build())

    // result is a Number directly
    root.getLong("result").getOrElse(
      root.getInt("result").map(_.toLong).getOrElse(0L)
    )
  }

  // ─── Partition List API (easymetahub) ──────────────────────────────────────

  /**
   * Get partition list for a table via GET /table/v1/partition/list (easymetahub).
   * Automatically paginates to collect all partitions.
   *
   * @param db        database name
   * @param table     table name
   * @param catalog   optional catalog (defaults to config)
   * @param partition optional partition filter (e.g. "dt=20250101")
   * @param sortBy    optional sort field
   * @param order     optional sort order ("asc" or "desc")
   * @param pageSize  items per page (default 500, max 500)
   * @return list of OpenApiPartitionInfo
   */
  def getPartitionList(db: String,
                       table: String,
                       catalog: Option[String] = None,
                       partition: Option[String] = None,
                       sortBy: Option[String] = None,
                       order: Option[String] = None,
                       pageSize: Int = 500): List[PartitionInfo] = {
    import JsonHelper.RichJsonNode
    val rawUrl = buildBaseUrl(MammutConfig.metaServiceName, "/table/v1/partition/list")

    val effectivePageSize = Math.min(pageSize, 500) // max 500
    val allItems = mutable.ListBuffer.empty[JsonNode]
    var pageNum = 1
    var hasMore = true

    while (hasMore) {
      val urlBuilder = HttpUrl.parse(rawUrl).newBuilder()
      appendAuthParams(urlBuilder)
      urlBuilder.addQueryParameter("catalog", catalog.getOrElse(MammutConfig.catalog))
      urlBuilder.addQueryParameter("db", db)
      urlBuilder.addQueryParameter("table", table)
      partition.foreach(p => urlBuilder.addQueryParameter("partition", p))
      sortBy.foreach(s => urlBuilder.addQueryParameter("sortBy", s))
      order.foreach(o => urlBuilder.addQueryParameter("order", o))
      urlBuilder.addQueryParameter("pageNum", pageNum.toString)
      urlBuilder.addQueryParameter("pageSize", effectivePageSize.toString)

      val root = executeGet(urlBuilder.build())

      // This API returns result as a direct array: { "result": [...] }
      val resultNode = root.get("result")
      val items: List[JsonNode] = if (resultNode != null && resultNode.isArray) {
        resultNode.asScala.toList
      } else {
        // Fallback: try nested { "result": { "list": [...] } }
        root.getObject("result").flatMap(_.getArray("list")).getOrElse(List.empty)
      }

      allItems ++= items
      hasMore = items.size >= effectivePageSize
      pageNum += 1
    }

    /** Helper: parse a string field as Long (metadata fields are strings like "22681684727"). */
    def parseStringLong(obj: JsonNode, key: String): Option[Long] = {
      obj.getString(key).flatMap { s =>
        try Some(s.toLong) catch { case _: NumberFormatException => None }
      }.orElse(obj.getLong(key))
    }

    allItems.toList.filter(_.isObject).map { obj =>
      // metadata is a nested object containing totalSize, numRows, numFiles as strings
      val metadata = obj.getObject("metadata")
      // serde contains location info under "uri"
      val serde = obj.getObject("serde")

      PartitionInfo(
        catalog = obj.getString("catalog"),
        db = obj.getString("db"),
        table = obj.getString("table"),
        partition = obj.getString("partition"),
        createTime = obj.getString("createTime"),
        updateTime = obj.getString("updateTime"),
        totalSize = metadata.flatMap(m => parseStringLong(m, "totalSize")),
        numFiles = metadata.flatMap(m => parseStringLong(m, "numFiles")),
        numRows = metadata.flatMap(m => parseStringLong(m, "numRows")),
        location = serde.flatMap(_.getString("uri")),
        rawJson = Some(obj)
      )
    }
  }

  /**
   * Gracefully shutdown the service, releasing all resources.
   */
  private def shutdown(): Unit = {
    httpClient.dispatcher().executorService().shutdown()
    httpClient.connectionPool().evictAll()
  }

  override def close(): Unit = shutdown()
}
