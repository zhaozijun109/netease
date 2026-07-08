package com.netease.bdms.ndi.service.web.param.task;

import com.netease.bdms.ndi.service.web.util.TaskConstant;
import lombok.Data;

/**
 * @ClassName GetTaskOwnerParam
 * @Description 获取任务负责人参数
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class GetTaskOwnerParam {
  /**
   * 任务类型
   * @see TaskConstant.TaskTypeEnum
   */
  private Integer taskType;
  /**
   * 页码（非必传）
   */
  private Integer pageNum;
  private Integer pageSize;
}
