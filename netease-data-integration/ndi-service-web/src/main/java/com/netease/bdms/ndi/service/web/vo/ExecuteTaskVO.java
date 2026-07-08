package com.netease.bdms.ndi.service.web.vo;

import com.netease.bdms.ndi.service.web.param.User;
import lombok.Data;

import java.util.List;

/**
 * @ClassName ExecuteTaskVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ExecuteTaskVO {
  private String taskId;
  private Long readerId;
  private Long writerId;
  private User user;
  private String name;
  private String description;
  //private String transferType;
  private String readerType;
  private Object reader;
  private String writerType;
  private Object writer;
  private List<Object> handlers;
}
