package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Data;

import java.util.List;

/**
 * @ClassName GetDataSourcesVO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class ListDataSourcesDto {
  private Integer code;
  private String message;
  private Integer total;
  private List<DataSourceDto> dataSourceDtoList;
}
