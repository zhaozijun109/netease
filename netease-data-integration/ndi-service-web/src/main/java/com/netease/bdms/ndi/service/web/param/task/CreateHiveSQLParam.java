package com.netease.bdms.ndi.service.web.param.task;

import com.netease.bdms.ndi.service.web.param.User;
import lombok.Data;

/**
 * @ClassName CreateHiveSQLParam
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class CreateHiveSQLParam {
  private User user;
  private String dataSourceId;
}
