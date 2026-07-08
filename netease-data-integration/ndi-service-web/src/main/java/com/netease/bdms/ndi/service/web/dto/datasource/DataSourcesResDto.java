package com.netease.bdms.ndi.service.web.dto.datasource;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName DataSourcesResDto
 * @Description 创建任务的数据源列表响应dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class DataSourcesResDto {
  private List<DataSourceSimpleDto> dataSources;

  public DataSourcesResDto() {
  }

  public DataSourcesResDto(List<DataSourceSimpleDto> dataSources) {
    this.dataSources = dataSources;
  }
}
