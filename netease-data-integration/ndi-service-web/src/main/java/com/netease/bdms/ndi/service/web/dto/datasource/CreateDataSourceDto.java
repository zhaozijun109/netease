package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Data;

/**
 * @ClassName CreateDataSourceDTO
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
@Data
public class CreateDataSourceDto {
  private Integer code;
  private String message;
  private DataSourceDto dataSourceDto;
}
