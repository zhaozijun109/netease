package com.netease.bdms.ndi.service.web.service;

import com.netease.bdms.ndi.service.web.controller.interceptor.Worker;
import com.netease.bdms.ndi.service.web.dto.AccountDto;
import com.netease.bdms.ndi.service.web.dto.ClusterDto;
import com.netease.bdms.ndi.service.web.dto.ProductClusterDto;
import com.netease.bdms.ndi.service.web.dto.user.UserDto;


import java.util.List;

/**
 * @ClassName UserService
 * @Description 用户服务
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface UserService {
  List<ProductClusterDto> listProductAndCluster(String email);

  void storeProductUsers(String product) throws Exception;

  String getProductUsername(String product, String email);

  Integer getProductId(String email, String product);

  List<UserDto> gerProductUsers(String product);

  String getClusterId(String email, String product, String cluster);

  String getUserName(String email);

  /**
   * 获取产品账号下的集群
   *
   * @param product 产品账号
   * @return
   */
  List<ClusterDto> listCurrentClusters(String product);

  /**
   * 切换产品账号和集群
   *
   * @param sessionId sessionId
   * @param email 用户email
   * @param product 产品账号
   * @param cluster 集群
   */
  Worker switchProductAndCluster(String sessionId, String email, String product, String cluster);

  AccountDto getAccountDtoByEmail(String email);

  /**
   * 列出用户所有的项目信息
   *
   * @param email 用户email
   * @return 项目账号列表
   */
  List<ProductClusterDto> listUserProduct(String email);
}
