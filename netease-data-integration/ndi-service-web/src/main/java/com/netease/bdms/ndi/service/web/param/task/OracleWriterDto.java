package com.netease.bdms.ndi.service.web.param.task;

import java.util.List;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @ClassName QSWriterDto
 * @Description DDB QS Writer
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class OracleWriterDto implements Writer {
  private JSONObject dataSource;
  private String insertType;
  private List<String> preSQL;
  private List<String> postSQL;
  private JSONArray conf;
}
