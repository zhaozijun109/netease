package com.netease.bdms.ndi.service.web.dto.datasource;

import lombok.Getter;
import lombok.Setter;

/**
 * @ClassName ConnectivityResultRspDto
 * @Description 数据源连通性结果请求dto
 * @Author Min Zhao
 * @Version 1.0
 **/
@Getter
@Setter
public class ConnectivityResultReqDto {
  private CheckAndDataSourceId dataSource;
}
