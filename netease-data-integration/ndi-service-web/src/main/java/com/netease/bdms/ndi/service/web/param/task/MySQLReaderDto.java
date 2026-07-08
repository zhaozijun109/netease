package com.netease.bdms.ndi.service.web.param.task;

import com.alibaba.fastjson.JSONArray;
import lombok.Data;



/**
 * @ClassName MySQLReader
 * @Description MySQLReader
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class MySQLReaderDto implements Reader {
  private JSONArray dataSources;
  private String conditions;
  private JSONArray conf;
}
