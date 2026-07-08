package com.netease.bdms.ndi.service.web.util;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName HiveTypes
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class HiveTypes {

  public static Map<String, Integer> getColumnTypes() {
    Map<String, Integer> columnTypes = new HashMap<>();
    columnTypes.put("TINYINT", Types.TINYINT);
    columnTypes.put("SMALLINT", Types.SMALLINT);
    columnTypes.put("INTEGER", Types.INTEGER);
    columnTypes.put("BIGINT", Types.BIGINT);
    columnTypes.put("BIT", Types.BIT);
    columnTypes.put("BOOLEAN", Types.BOOLEAN);
    columnTypes.put("REAL", Types.REAL);
    columnTypes.put("FLOAT", Types.FLOAT);
    columnTypes.put("DOUBLE", Types.DOUBLE);
    columnTypes.put("NUMERIC", Types.NUMERIC);
    columnTypes.put("DECIMAL", Types.DECIMAL);
    columnTypes.put("CHAR", Types.CHAR);
    columnTypes.put("VARCHAR", Types.VARCHAR);
    columnTypes.put("LONGVARCHAR", Types.LONGVARCHAR);
    columnTypes.put("LONGNVARCHAR", Types.LONGNVARCHAR);
    columnTypes.put("NVARCHAR", Types.NVARCHAR);
    columnTypes.put("NCHAR", Types.NCHAR);
    columnTypes.put("DATE", Types.DATE);
    columnTypes.put("BLOB", Types.BLOB);
    columnTypes.put("BINARY", Types.BINARY);
    columnTypes.put("VARBINARY", Types.VARBINARY);
    columnTypes.put("LONGVARBINARY", Types.LONGVARBINARY);
    columnTypes.put("DATETIME", Types.TIMESTAMP);
    columnTypes.put("GEOMETRY", Types.BINARY);
    columnTypes.put("GEOMETRYCOLLECTION", Types.BINARY);
    columnTypes.put("LINESTRING", Types.BINARY);
    columnTypes.put("LONGBLOB", Types.LONGVARBINARY);
    columnTypes.put("LONGTEXT", Types.LONGVARCHAR);
    columnTypes.put("MEDIUMBLOB", Types.LONGVARBINARY);
    columnTypes.put("MEDIUMINT", Types.INTEGER);
    columnTypes.put("MEDIUMTEXT", Types.LONGVARCHAR);
    columnTypes.put("MULTILINESTRING", Types.BINARY);
    columnTypes.put("MULTIPOINT", Types.BINARY);
    columnTypes.put("MULTIPOLYGON", Types.BINARY);
    columnTypes.put("POINT", Types.BINARY);
    columnTypes.put("POLYGON", Types.BINARY);
    columnTypes.put("TEXT", Types.LONGVARCHAR);
    columnTypes.put("TIME", Types.TIME);
    columnTypes.put("TIMESTAMP", Types.TIMESTAMP);
    columnTypes.put("TINYBLOB", Types.VARBINARY);
    columnTypes.put("TINYTEXT", Types.LONGVARCHAR);
    columnTypes.put("YEAR", Types.DATE);
    columnTypes.put("JSON", Types.CHAR);
    columnTypes.put("ENUM", Types.CHAR);
    columnTypes.put("SET", Types.CHAR);

    return columnTypes;
  }

  public static String toHiveTypeForParquet(int sqlType) {
    switch (sqlType) {
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
        return "INT";
      case Types.BIGINT:
        return "BIGINT";
      case Types.BIT:
      case Types.BOOLEAN:
        return "BOOLEAN";
      case Types.REAL:
        return "FLOAT";
      case Types.FLOAT:
      case Types.DOUBLE:
        return "DOUBLE";
      case Types.NUMERIC:
      case Types.DECIMAL:
        return "STRING";
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.LONGNVARCHAR:
      case Types.NVARCHAR:
      case Types.NCHAR:
        return "STRING";
      case Types.DATE:
      case Types.TIME:
      case Types.TIMESTAMP:
        return "BIGINT";
      case Types.BLOB:
      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
        return "BINARY";
      // ORACLE BINARY_FLOAT
      case 100:
        return "FLOAT";
      // ORACLE BINARY_DOUBLE
      case 101:
        return "DOUBLE";
      default:
        throw new IllegalArgumentException("Cannot convert SQL type "
          + sqlType);
    }
  }
}
