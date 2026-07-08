package com.netease.bdms.ndi.service.web.param.task;

import com.alibaba.fastjson.JSONArray;
import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName QSReader
 * @Description DDB QS Reader
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class QSReaderDto implements Reader {
  private JSONArray dataSources;
  private String conditions;
  private JSONArray conf;
}
