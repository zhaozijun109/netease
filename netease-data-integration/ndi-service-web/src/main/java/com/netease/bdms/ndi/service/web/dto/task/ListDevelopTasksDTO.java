package com.netease.bdms.ndi.service.web.dto.task;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ListTasksDTO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ListDevelopTasksDTO {
  private Integer total;
  private List<DevelopTaskDTO> developTaskDTOList;
}
