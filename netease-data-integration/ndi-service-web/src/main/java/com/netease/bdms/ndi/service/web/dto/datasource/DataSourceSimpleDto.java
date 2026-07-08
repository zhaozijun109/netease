package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @ClassName DataSourceSimpleDto
 * @Description 数据源id和name
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
@ToString
public class DataSourceSimpleDto {
  private Long id;
  private String name;

  public DataSourceSimpleDto() {
  }

  public DataSourceSimpleDto(Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
