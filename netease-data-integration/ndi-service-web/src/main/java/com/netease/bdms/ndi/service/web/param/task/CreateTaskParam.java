package com.netease.bdms.ndi.service.web.param.task;


import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @ClassName CreateTaskParam
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class CreateTaskParam {
  private String name;
  private String description;
  private String readerType;
  private JSONObject reader;
  private String writerType;
  private JSONObject writer;
  private List<Object> handlers;
}
