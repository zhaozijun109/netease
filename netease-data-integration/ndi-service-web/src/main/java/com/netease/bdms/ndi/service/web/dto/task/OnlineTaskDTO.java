package com.netease.bdms.ndi.service.web.dto.task;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName OnlineTaskDTO
 * @Description 线上任务dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class OnlineTaskDTO {
  private Long id;
  private String taskName;
  private String readerType;
  private String writerType;
  private String owner;
  private String modifyTime;
  private Object reader;
  private Object writer;
  private String createTime;
  private String modifier;
  private String creator;
  private String taskId;
}
