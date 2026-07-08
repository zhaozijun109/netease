package com.netease.yuanqi.doris.schema

import org.slf4j.LoggerFactory

/**
 * Maps Hive column types to Doris column types.
 *
 * The mapping is derived from the Spark ↔ Doris type correspondence:
 *
 *   SparkType       DorisType
 *   ─────────       ─────────
 *   BooleanType     BOOLEAN
 *   ShortType       SMALLINT
 *   IntegerType     INT
 *   LongType        BIGINT
 *   FloatType       FLOAT
 *   DoubleType      DOUBLE
 *   DecimalType     DECIMAL(p,s)
 *   StringType      STRING
 *   DateType        DATE
 *   TimestampType   DATETIME
 *   ArrayType       ARRAY<T>
 *   MapType         MAP<K,V>
 *   StructType      STRUCT<...>
 *
 * Since Hive types map naturally to Spark types, the final chain is:
 *   Hive Type → Spark Type → Doris Type
 *
 * Doris 3.x type constraints:
 *   - DECIMAL max precision = 38, max scale = precision
 *   - CHAR max length = 255
 *   - VARCHAR max length = 65533
 *   - STRING is an alias for VARCHAR(65533)
 *   - No native BINARY support (fallback to STRING)
 *   - UNIONTYPE not supported (fallback to STRING)
 *
 * Usage:
 * {{{
 *   val dorisType = DorisTypeMapper.mapType("decimal(18,2)")  // => "DECIMAL(18, 2)"
 *   val dorisType = DorisTypeMapper.mapType("array<string>")  // => "ARRAY<STRING>"
 *   val dorisType = DorisTypeMapper.mapType("map<string,int>") // => "MAP<STRING, INT>"
 * }}}
 */
object DorisTypeMapper {

  private val LOG = LoggerFactory.getLogger(getClass)

  // ────────────────────────────────────────────
  //  Regex patterns for parameterized types
  // ────────────────────────────────────────────

  /** Matches decimal(precision, scale) with optional whitespace. */
  private val DecimalPattern = """(?i)decimal\s*\(\s*(\d+)\s*,\s*(\d+)\s*\)""".r

  /** Matches char(n) or varchar(n). */
  private val CharPattern = """(?i)(char|varchar)\s*\(\s*(\d+)\s*\)""".r

  /** Matches array<elementType>. */
  private val ArrayPattern = """(?i)array\s*<\s*(.+)\s*>""".r

  /** Matches map<keyType, valueType> — bracket-aware split handled inside. */
  private val MapPattern = """(?i)map\s*<\s*(.+)\s*>""".r

  /** Matches struct<field1:type1, field2:type2, ...>. */
  private val StructPattern = """(?i)struct\s*<\s*(.+)\s*>""".r

  /** Matches union type<...> — Hive-specific, no direct Doris support. */
  private val UnionPattern = """(?i)uniontype\s*<.*>""".r

  // ────────────────────────────────────────────
  //  Simple (non-parameterized) type mapping
  // ────────────────────────────────────────────

  /**
   * Direct mapping for simple Hive types to Doris types.
   *
   * Mapping chain: Hive → Spark → Doris
   *   boolean   → BooleanType   → BOOLEAN
   *   tinyint   → ShortType     → SMALLINT   (Doris has TINYINT but Spark widens to Short)
   *   smallint  → ShortType     → SMALLINT
   *   int       → IntegerType   → INT
   *   bigint    → LongType      → BIGINT
   *   float     → FloatType     → FLOAT
   *   double    → DoubleType    → DOUBLE
   *   string    → StringType    → STRING
   *   binary    → BinaryType    → STRING     (no Doris BINARY)
   *   date      → DateType      → DATE
   *   timestamp → TimestampType → DATETIME
   *   decimal   → DecimalType   → DECIMAL(27, 9)  (bare decimal without precision)
   *
   * All keys are lowercase.
   */
  private val simpleTypeMap: Map[String, String] = Map(
    // Boolean
    "boolean"   -> "BOOLEAN",

    // Integer types  (Hive → Spark → Doris)
    "tinyint"   -> "TINYINT",
    "smallint"  -> "SMALLINT",
    "int"       -> "INT",
    "integer"   -> "INT",
    "bigint"    -> "BIGINT",

    // Floating-point types
    "float"     -> "FLOAT",
    "double"    -> "DOUBLE",

    // String types
    "string"    -> "STRING",
    "binary"    -> "STRING",         // Doris has no BINARY; fallback to STRING

    // Date / time types
    "date"      -> "DATE",
    "timestamp" -> "DATETIME",       // Hive TIMESTAMP → Spark TimestampType → Doris DATETIME

    // Bare decimal without explicit precision — default to DECIMAL(27, 9)
    "decimal"   -> "DECIMAL(27, 9)"
  )

  // ────────────────────────────────────────────
  //  Public API
  // ────────────────────────────────────────────

  /**
   * Convert a Hive column type string to a Doris column type string.
   *
   * Supports:
   *  - Simple types: string, int, bigint, boolean, date, timestamp, etc.
   *  - Parameterized types: decimal(p,s), char(n), varchar(n)
   *  - Complex types: array<T>, map<K,V>, struct<...>
   *  - Unknown types fallback to STRING with a warning log.
   *
   * @param hiveType the Hive column type (case-insensitive)
   * @return the corresponding Doris column type
   */
  def mapType(hiveType: String): String = {
    val trimmed = hiveType.trim
    val lower = trimmed.toLowerCase

    // 1. Try simple type map first
    simpleTypeMap.get(lower) match {
      case Some(dorisType) => return dorisType
      case None            => // fall through
    }

    // 2. Parameterized / complex types
    trimmed match {

      // ── decimal(p, s) ──
      case DecimalPattern(p, s) =>
        val precision = p.toInt
        val scale = s.toInt
        // Doris DECIMAL max precision = 38, max scale = precision
        val clampedP = math.min(precision, 38)
        val clampedS = math.min(scale, clampedP)
        return s"DECIMAL($clampedP, $clampedS)"

      // ── char(n) / varchar(n) ──
      case CharPattern(typeName, length) =>
        val n = length.toInt
        typeName.toLowerCase match {
          case "char" =>
            // Doris CHAR max length = 255
            val clampedN = math.min(n, 255)
            return s"CHAR($clampedN)"
          case "varchar" =>
            // Doris VARCHAR max length = 65533
            val clampedN = math.min(n, 65533)
            return s"VARCHAR($clampedN)"
        }

      // ── array<elementType> ──
      case ArrayPattern(elementType) =>
        val dorisElem = mapType(elementType)
        return s"ARRAY<$dorisElem>"

      // ── map<keyType, valueType> ──
      // Use bracket-aware split to handle nested generics like map<string, array<int>>
      case MapPattern(inner) =>
        val parts = splitTopLevelComma(inner)
        if (parts.size >= 2) {
          val dorisKey = mapType(parts.head)
          val dorisValue = mapType(parts.tail.mkString(",").trim)
          return s"MAP<$dorisKey, $dorisValue>"
        } else {
          LOG.warn(s"Cannot parse MAP type definition: $hiveType, mapping to JSON")
          return "JSON"
        }

      // ── struct<field1:type1, field2:type2, ...> ──
      case StructPattern(fieldsDef) =>
        val fields = splitTopLevelComma(fieldsDef)
        val dorisFields = fields.map { fieldDef =>
          val colonIdx = fieldDef.indexOf(':')
          if (colonIdx > 0) {
            val fieldName = fieldDef.substring(0, colonIdx).trim
            val fieldType = fieldDef.substring(colonIdx + 1).trim
            s"$fieldName:${mapType(fieldType)}"
          } else {
            fieldDef.trim
          }
        }
        return s"STRUCT<${dorisFields.mkString(", ")}>"

      // ── uniontype<...> — not supported ──
      case UnionPattern() =>
        LOG.warn(s"Hive UNIONTYPE is not supported in Doris, mapping to STRING: $hiveType")
        return "STRING"

      case _ => // fall through to default
    }

    // 3. Fallback: unknown type → STRING
    LOG.warn(s"Unknown Hive type '$hiveType', mapping to STRING")
    "STRING"
  }

  // ────────────────────────────────────────────
  //  Internal helpers
  // ────────────────────────────────────────────

  /**
   * Split a type-parameter string by comma at the top level only,
   * respecting angle-bracket and parenthesis nesting.
   *
   * Examples:
   *   "string, int"                          → ["string", "int"]
   *   "string, array<int>"                   → ["string", "array<int>"]
   *   "string, map<string, int>"             → ["string", "map<string, int>"]
   *   "a:int, b:map<string,int>, c:string"   → ["a:int", "b:map<string,int>", "c:string"]
   */
  def splitTopLevelComma(input: String): List[String] = {
    val parts = scala.collection.mutable.ListBuffer.empty[String]
    var depth = 0
    var start = 0

    for (i <- input.indices) {
      input.charAt(i) match {
        case '<' | '(' => depth += 1
        case '>' | ')' => depth -= 1
        case ',' if depth == 0 =>
          parts += input.substring(start, i).trim
          start = i + 1
        case _ =>
      }
    }
    // Don't forget the last segment
    if (start <= input.length) {
      val last = input.substring(start).trim
      if (last.nonEmpty) {
        parts += last
      }
    }

    parts.toList
  }
}
