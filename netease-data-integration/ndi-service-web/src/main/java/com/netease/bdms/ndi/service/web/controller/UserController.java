package com.netease.bdms.ndi.service.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.controller.interceptor.SessionHandler;
import com.netease.bdms.ndi.service.web.controller.interceptor.Worker;
import com.netease.bdms.ndi.service.web.dto.ClusterDto;
import com.netease.bdms.ndi.service.web.dto.ProductAndClusterDto;
import com.netease.bdms.ndi.service.web.dto.ProductClusterDto;
import com.netease.bdms.ndi.service.web.dto.user.UserDto;
import com.netease.bdms.ndi.service.web.dto.user.UserDtoListDto;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.service.impl.MammutMetaService;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.util.*;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @ClassName UserController
 * @Description 用户Controller
 * @Author Min Zhao
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/api/v1/user")
public class UserController {
  @Autowired
  private UserService userService;

  /**
   * 获取项目下所有的用户
   *
   * @return
   */
  @GetMapping(value = "/product/users")
  public ResponseResult<UserDtoListDto> listProductUsers(){
    String product = NdiContext.get(ContextConstant.PRODUCT);
    List<UserDto> userDtoList = userService.gerProductUsers(product);
    UserDtoListDto userDtoListDto = new UserDtoListDto(userDtoList);
    return ResponseResult.createBySuccess(userDtoListDto);
  }

  /**
   * 获取用户加入的项目和集群
   *
   * @return
   */
  @GetMapping(value = "/productsAndClusters")
  public ResponseResult<List<ProductClusterDto>> listProductCluster() {
    String email = NdiContext.get(ContextConstant.EMAIL);
    List<ProductClusterDto> productClusterDtoList = userService.listUserProduct(email);
    return ResponseResult.createBySuccess(productClusterDtoList);
  }

  /**
   * 选择项目账号和集群
   *
   * @param productAndCluster
   * @param request
   * @return
   */
  @PostMapping(value = "/selectProductAndCluster")
  public ResponseResult switchProductAndCluster(@Validated @RequestBody ProductAndClusterDto productAndCluster, HttpServletRequest request) {

    String product = productAndCluster.getProduct();
    String cluster = productAndCluster.getCluster();
    String email = NdiContext.get(ContextConstant.EMAIL);
    String sessionId = CookieUtil.get(request, CommonConstants.REDIS_SESSION_ID);
    userService.switchProductAndCluster(sessionId, email, product, cluster);
    return ResponseResult.createBySuccess();
  }

  /**
   * 获取用户当前项目的集群列表
   *
   * @return 集群列表
   */
  @GetMapping(value = "/currentClusters")
  public ResponseResult<List<ClusterDto>> listCurrentClusters() {
    String product = NdiContext.get(ContextConstant.PRODUCT);
    List<ClusterDto> clusterDtoList = userService.listCurrentClusters(product);
    return ResponseResult.createBySuccess(clusterDtoList);
  }
}
