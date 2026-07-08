package com.netease.bdms.ndi.service.web.dto.task;

import com.alibaba.fastjson.JSONArray;
import com.netease.bdms.ndi.service.web.param.User;
import lombok.Data;

import java.util.List;

/**
 * @ClassName DevelopTaskDTO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class TaskDTO {
  private String taskId;
  private Long readerId;
  private Long writerId;
  private User user;
  private String name;
  private String description;
  private String readerType;
  private Object reader;
  private String writerType;
  private Object writer;
  private List<Object> handlers;
}
