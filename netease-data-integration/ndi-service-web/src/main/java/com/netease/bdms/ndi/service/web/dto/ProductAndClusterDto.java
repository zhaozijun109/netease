package com.netease.bdms.ndi.service.web.dto;

import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName ProductAndClusterDto
 * @Description 项目和集群dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class ProductAndClusterDto {

  /**
   * 项目账号
   */
  @NotNull(message = "product不能为空")
  private String product;

  /**
   * 集群名称
   */
  @NotNull(message = "cluster不能为空")
  private String cluster;

}
