package com.netease.bdms.ndi.service.web.param.task;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;

/**
 * @ClassName DBIReader
 * @Description DDB DBI Reader
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class DBIReaderDto implements Reader {
  /**
   * dataSourceId
   * database
   * table
   * tableNameType
   * catalogName
   */
  private JSONArray dataSources;
  private String conditions;
  private JSONArray conf;
}
