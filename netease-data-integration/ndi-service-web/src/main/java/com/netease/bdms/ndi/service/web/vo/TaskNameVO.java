package com.netease.bdms.ndi.service.web.vo;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.dto.task.TaskNameDTO;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName TaskNameVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class TaskNameVO {
  private String taskId;
  private String taskName;

  public static TaskNameVO taskNameDTO2VO(TaskNameDTO taskNameDTO) {
    ParamUtil.validate(taskNameDTO);
    TaskNameVO taskNameVO = new TaskNameVO();
    BeanUtils.copyProperties(taskNameDTO, taskNameVO);
    return taskNameVO;
  }

  public static List<TaskNameVO> json2TaskNameVO(JSONObject jsonObject) {
    ParamUtil.validate(jsonObject);
    List<TaskNameDTO> listTaskNamesDTO = (List<TaskNameDTO>) jsonObject.get("taskNames");
    List<TaskNameVO> taskNameVOList = listTaskNamesDTO.parallelStream()
      .map(item -> TaskNameVO.taskNameDTO2VO(item))
      .collect(Collectors.toList());
    return taskNameVOList;
  }
}
