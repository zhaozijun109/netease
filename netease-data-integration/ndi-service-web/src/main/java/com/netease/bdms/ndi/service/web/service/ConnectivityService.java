package com.netease.bdms.ndi.service.web.service;

import java.util.List;

import com.netease.bdms.ndi.service.web.dto.datasource.CheckAndDataSourceId;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityResultRspDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusReqDto;
import com.netease.bdms.ndi.service.web.dto.datasource.ConnectivityStatusResDto;

/**
 * @ClassName ConnectivityService
 * @Description 连通性检测服务
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface ConnectivityService {

  /**
   * 数据源连通性检测
   *
   * @param connectivityReqDto 请求
   */
  CheckAndDataSourceId execute(ConnectivityReqDto connectivityReqDto);

  /**
   * 获取连通性检测的状态
   *
   * @param connectivityStatusReqDto
   * @return
   */
  List<ConnectivityStatusResDto> status(ConnectivityStatusReqDto connectivityStatusReqDto);

  /**
   * 获取连通性检测结果
   *
   * @param connectivityResultReqDto
   * @return
   */
  ConnectivityResultRspDto result(ConnectivityResultReqDto connectivityResultReqDto);

}
