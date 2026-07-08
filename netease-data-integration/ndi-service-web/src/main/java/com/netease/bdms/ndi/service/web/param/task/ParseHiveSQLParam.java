package com.netease.bdms.ndi.service.web.param.task;

import com.netease.bdms.ndi.service.web.param.User;
import lombok.Data;

/**
 * @ClassName ParseHiveSQLParam
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ParseHiveSQLParam {
  private User user;
  private String hiveSQL;
}
