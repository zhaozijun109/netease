package com.netease.bdms.ndi.service.web.param.task;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.util.List;

/**
 * @ClassName ModifyTaskParam
 * @Description 更新任务参数
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ModifyTaskParam {
  private String taskId;
  private Long readerId;
  private Long writerId;
  private String name;
  private String description;
  private String readerType;
  private JSONObject reader;
  private String writerType;
  private JSONObject writer;
  private List<Object> handlers;


}
