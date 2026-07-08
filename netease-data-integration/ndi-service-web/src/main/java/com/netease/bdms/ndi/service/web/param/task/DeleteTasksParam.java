package com.netease.bdms.ndi.service.web.param.task;

import lombok.Data;

import java.util.List;

/**
 * @ClassName DeleteTasksParam
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class DeleteTasksParam {
  Integer taskType;
  private List<String> taskIds;
}
