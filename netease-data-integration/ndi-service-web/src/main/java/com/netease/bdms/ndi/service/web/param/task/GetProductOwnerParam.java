package com.netease.bdms.ndi.service.web.param.task;

import com.netease.bdms.ndi.service.web.param.User;
import lombok.Data;

/**
 * @ClassName GetProductOwnerParam
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class GetProductOwnerParam {
  private User user;
  private Byte taskType;
}
