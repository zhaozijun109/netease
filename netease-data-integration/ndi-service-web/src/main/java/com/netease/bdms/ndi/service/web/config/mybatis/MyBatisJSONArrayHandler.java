package com.netease.bdms.ndi.service.web.config.mybatis;

import com.alibaba.fastjson.JSONArray;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @ClassName MyBatisJSONArrayHandler
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@MappedTypes(JSONArray.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class MyBatisJSONArrayHandler extends BaseTypeHandler<JSONArray> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, JSONArray parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, String.valueOf(parameter.toJSONString()));
  }

  @Override
  public JSONArray getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String sqlJSON = rs.getString(columnName);
    if (null != sqlJSON) {
      return JSONArray.parseArray(sqlJSON);
    }
    return null;
  }

  @Override
  public JSONArray getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String sqlJson = rs.getString(columnIndex);
    if (null != sqlJson) {
      return JSONArray.parseArray(sqlJson);
    }
    return null;
  }

  @Override
  public JSONArray getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String sqlJson = cs.getString(columnIndex);
    if (null != sqlJson) {
      return JSONArray.parseArray(sqlJson);
    }
    return null;
  }
}
