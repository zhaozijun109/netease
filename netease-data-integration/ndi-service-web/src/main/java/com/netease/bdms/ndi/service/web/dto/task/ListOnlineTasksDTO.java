package com.netease.bdms.ndi.service.web.dto.task;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ListOnlineTasksDTO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ListOnlineTasksDTO {
  private Integer total;
  private List<OnlineTaskDTO> onlineTaskDTOList;
}
