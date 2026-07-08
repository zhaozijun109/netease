package com.netease.bdms.ndi.service.web.vo;

import lombok.Data;

import java.util.List;

/**
 * @ClassName TaskOwnerVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class TaskOwnerVO {
  private List<UserVO> userVOList;
  private Integer pageSize;
  private Integer pageNum;
  private Integer total;
}
