package com.netease.bdms.ndi.service.web.dto.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.param.User;
import lombok.Data;

import java.util.List;

/**
 * @ClassName ExecuteTaskDTO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ExecuteTaskDTO {
  private String taskId;
  private Long readerId;
  private Long writerId;
  private User user;
  private String name;
  private String description;
  //private String transferType;
  private String readerType;
  private JSONObject reader;
  private String writerType;
  private JSONObject writer;
  private JSONArray handlers;
}
