package com.netease.bdms.ndi.service.web.param.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName HiveReader
 * @Description HiveReader
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class HiveReaderDto implements Reader {
  /**
   * 数据源信息
   * dataSourceId
   * database
   * table
   * catalogName
   */
  private JSONObject dataSource;
  /**
   * where条件
   */
  private String conditions;
  /**
   * 高级配置
   */
  private JSONArray conf;

  @Override
  public JSONArray getDataSources() {
    JSONArray dataSources = new JSONArray();
    dataSources.add(getDataSource());
    return dataSources;
  }
}
