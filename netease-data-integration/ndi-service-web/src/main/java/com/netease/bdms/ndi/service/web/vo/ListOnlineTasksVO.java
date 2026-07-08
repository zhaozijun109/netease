package com.netease.bdms.ndi.service.web.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName ListTasksVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ListOnlineTasksVO {
  private List<OnlineTaskVO> tasks;
  private Integer pageNum;
  private Integer pageSize;
  private Integer total;
}
