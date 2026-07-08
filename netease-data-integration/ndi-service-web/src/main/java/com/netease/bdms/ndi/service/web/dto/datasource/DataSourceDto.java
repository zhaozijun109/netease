package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Data;

@Data
public class DataSourceDto {
  private Long id;
  private String type;
  private String modifier;
  private String creator;
  private String name;
  private Object connectionInformation;
  private String createTime;
  private String modifyTime;
}
