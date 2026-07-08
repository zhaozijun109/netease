package com.netease.bdms.ndi.service.web.param.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @ClassName QSWriterDto
 * @Description DDB QS Writer
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class QSWriterDto implements Writer {
  private JSONObject dataSource;
  private String insertType;
  private List<String> preSQL;
  private List<String> postSQL;
  private JSONArray conf;
}
