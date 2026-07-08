package com.netease.bdms.ndi.service.web.config.mybatis;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @ClassName MyBatisJSONHandler
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@MappedTypes(JSONObject.class)
@MappedJdbcTypes(JdbcType.VARCHAR)
public class MyBatisJSONHandler extends BaseTypeHandler<JSONObject> {
  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, JSONObject parameter, JdbcType jdbcType) throws SQLException {
    ps.setString(i, String.valueOf(parameter.toJSONString()));
  }

  @Override
  public JSONObject getNullableResult(ResultSet rs, String columnName) throws SQLException {
    String sqlJSON = rs.getString(columnName);
    if (null != sqlJSON) {
      return JSONObject.parseObject(sqlJSON);
    }
    return null;
  }

  @Override
  public JSONObject getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
    String sqlJson = rs.getString(columnIndex);
    if (null != sqlJson) {
      return JSONObject.parseObject(sqlJson);
    }
    return null;
  }

  @Override
  public JSONObject getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
    String sqlJson = cs.getString(columnIndex);
    if (null != sqlJson) {
      return JSONObject.parseObject(sqlJson);
    }
    return null;
  }
}
