package com.netease.bdms.ndi.service.web.param.task;

import com.netease.bdms.ndi.service.web.param.User;
import lombok.Data;

/**
 * @ClassName GetTaskEditInformation
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class GetTaskEditInformationParam {
  private User user;
  private String taskId;
  private String taskType;
}
