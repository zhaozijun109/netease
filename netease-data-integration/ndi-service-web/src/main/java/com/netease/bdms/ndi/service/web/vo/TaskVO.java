package com.netease.bdms.ndi.service.web.vo;

import com.netease.bdms.ndi.service.web.dto.task.TaskDTO;
import com.netease.bdms.ndi.service.web.param.User;
import com.netease.bdms.ndi.service.web.util.ParamUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.List;

/**
 * @ClassName DevelopTaskVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class TaskVO {
  private String taskId;
  private Long readerId;
  private Long writerId;
  private User user;
  private String name;
  private String description;
  private String readerType;
  private Object reader;
  private String writerType;
  private Object writer;
  private List<Object> handlers;

  public static TaskVO developTaskDTO2VO(TaskDTO taskDTO) {
    ParamUtil.validate(taskDTO);
    TaskVO taskVO = new TaskVO();
    BeanUtils.copyProperties(taskDTO, taskVO);
    return taskVO;
  }
}
