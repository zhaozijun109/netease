package com.netease.bdms.ndi.service.web.param.task;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName ListTasksParam
 * @Description 获取任务列表请求dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class ListTasksParam {
  private String readerType;
  private String writerType;
  private Integer pageNum;
  private Integer pageSize;
  private String searchType;
  private String searchKey;
  private String sortType;
  private String sortBy;
}
