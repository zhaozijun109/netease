package com.netease.bdms.ndi.service.web.vo;

import com.netease.bdms.ndi.service.web.dto.task.TaskNameDTO;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * @ClassName TaskNamesVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ListTaskNamesVO {
  private Integer pageNum;
  private Integer pageSize;
  private Integer total;

  private Set<TaskNameDTO> taskNameVOList;
}
