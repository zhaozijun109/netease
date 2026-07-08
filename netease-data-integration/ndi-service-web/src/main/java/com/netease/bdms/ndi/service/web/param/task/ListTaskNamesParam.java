package com.netease.bdms.ndi.service.web.param.task;

import com.netease.bdms.ndi.service.web.param.User;
import lombok.Data;

/**
 * @ClassName ListTaskNamesParam
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ListTaskNamesParam {
  private User user;
  private String taskName;
  private Integer pageNum;
  private Integer pageSize;
}
