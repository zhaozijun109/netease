package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName DataSourceResDto
 * @Description 数据源id和name响应dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class DataSourceResDto {
  private DataSourceSimpleDto dataSource;

  public DataSourceResDto() {
  }

  public DataSourceResDto(DataSourceSimpleDto dataSource) {
    this.dataSource = dataSource;
  }
}
