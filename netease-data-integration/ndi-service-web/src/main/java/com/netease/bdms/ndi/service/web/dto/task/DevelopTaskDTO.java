package com.netease.bdms.ndi.service.web.dto.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName DevelopTaskDTO
 * @Description 开发任务dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class DevelopTaskDTO {
  private String taskName;
  private String readerType;
  private String writerType;
  private Object reader;
  private Object writer;
  private String owner;
  private String taskStatus;
  private String modifyTime;
  private String createTime;
  private String modifier;
  private String creator;
  private String taskId;
}
