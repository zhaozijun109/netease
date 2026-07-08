package com.netease.bdms.ndi.service.web.param.task;

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName OracleReaderDto
 * @Description Oracle reader
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class OracleReaderDto implements Reader {
  private JSONArray dataSources;
  private String conditions;
  private JSONArray conf;
}
