package com.netease.bdms.ndi.service.web.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName AzkabanJobDto
 * @Description Azkaban Job关联dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class AzkabanJobDto {
  private String jobId;
  private String product;
  private String projectName;
  private Integer productId;
  private String flowId;
  private String flowAliasName;
  private String taskId;
  private String projectAliasName;
  private String flowOwner;


  public boolean isDevJob() {
    return projectName.endsWith("$$dev");
  }
}
