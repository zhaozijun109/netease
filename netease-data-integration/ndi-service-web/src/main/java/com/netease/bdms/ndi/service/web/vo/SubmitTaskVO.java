package com.netease.bdms.ndi.service.web.vo;

import com.netease.bdms.ndi.service.web.dto.task.SubmitTaskDTO;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * @ClassName EditTaskVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class SubmitTaskVO {
  private String taskId;

  public static SubmitTaskVO submitTaskDTO2VO(SubmitTaskDTO submitTaskDTO) {
    ParamUtil.validate(submitTaskDTO);
    SubmitTaskVO submitTaskVO = new SubmitTaskVO();
    BeanUtils.copyProperties(submitTaskDTO, submitTaskVO);
    return submitTaskVO;
  }
}
