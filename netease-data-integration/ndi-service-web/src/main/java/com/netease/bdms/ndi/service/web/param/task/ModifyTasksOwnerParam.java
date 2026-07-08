package com.netease.bdms.ndi.service.web.param.task;

import com.netease.bdms.ndi.service.web.param.User;
import lombok.Data;

import java.util.List;

/**
 * @ClassName ModifyTaskOwnerParam
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ModifyTasksOwnerParam {
  private User user;
  private List<String> taskIds;
  private String newOwner;
}
