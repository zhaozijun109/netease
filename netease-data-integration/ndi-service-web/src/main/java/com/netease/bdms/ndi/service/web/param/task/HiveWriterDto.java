package com.netease.bdms.ndi.service.web.param.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;


/**
 * @ClassName HiveWriter
 * @Description Hive Writer
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class HiveWriterDto implements Writer {
  private JSONObject dataSource;
  private String insertType;
  private JSONArray conf;
  private JSONArray partitionList;

}
