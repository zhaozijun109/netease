package com.netease.bdms.ndi.service.web.facade;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.controller.interceptor.SessionHandler;
import com.netease.bdms.ndi.service.web.controller.interceptor.Worker;
import com.netease.bdms.ndi.service.web.exception.LoginException;
import com.netease.bdms.ndi.service.web.helper.AacHelper;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.service.impl.MammutMetaService;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import com.netease.bdms.ndi.service.web.util.RedisUtil;
import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

import java.util.List;
import java.util.stream.Collectors;


/**
 * 认证facade层
 *
 * @author
 * @create 2019-10-28 8:33 下午
 */
@Slf4j
@Component
public class AuthFacade {
  @Autowired
  private SessionHandler sessionHandler;
  @Autowired
  private AacHelper aacHelper;
  @Autowired
  private MammutMetaService mammutMetaService;
  @Autowired
  private RedisUtil redisUtil;
  @Autowired
  private UserService userService;

  /**
   * 登录
   *
   * @param indexUrl 登录后的首页地址（已编码）
   * @return 登录地址
   */
  public String login(String indexUrl) throws UnsupportedEncodingException {
    return aacHelper.buildLoginUrl(indexUrl);
  }

  /**
   * 登出
   *
   * @param token token
   * @return 登出地址
   */
  public String logout(String token) throws UnsupportedEncodingException {
    sessionHandler.deleteSession(token);
    return aacHelper.buildLogoutUrl();
  }

  /**
   * 回调验证
   * TODO: 换成accountByEmail接口
   *
   * @param token AAC分配的token
   */
  public void verify(String token) {
    Worker worker = aacHelper.getUser(token);
    JSONObject accounts = mammutMetaService.getAccount(worker.getEmail());
    List<String> products = accounts.getJSONArray("result").toJavaList(String.class);
    List<String> notBlankProducts = products.stream()
        .filter(item -> StringUtils.isNotBlank(item)).collect(Collectors.toList());

    String lastProduct = null;
    String lastCluster = null;
    try {
      lastProduct = redisUtil.hget(redisUtil.keyBuilder(CommonConstants.REDIS_USER_INFO, worker.getEmail()),
          CommonConstants.REDIS_USER_PRODUCT);
      lastCluster = redisUtil.hget(redisUtil.keyBuilder(CommonConstants.REDIS_USER_INFO, worker.getEmail()),
          CommonConstants.REDIS_USER_CLUSTER);
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    if (StringUtils.isNotBlank(lastProduct) && StringUtils.isNotBlank(lastCluster)) {
      List<String> clusterNames = mammutMetaService.getClusterName(lastProduct);
      List<String> notBlankClusterNames = clusterNames.stream()
          .filter(item -> StringUtils.isNotBlank(item))
          .collect(Collectors.toList());
      if (notBlankClusterNames != null && notBlankClusterNames.size() != 0 && notBlankClusterNames.contains(lastCluster)) {
        worker.setProduct(lastProduct);
        worker.setCluster(lastCluster);
      }
    }
    if (worker.getProduct() == null && worker.getCluster() == null) {
      for (String product : notBlankProducts) {
        List<String> clusterNames = mammutMetaService.getClusterName(product);
        List<String> notBlankClusterNames = clusterNames.stream()
            .filter(item -> StringUtils.isNotBlank(item))
            .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(notBlankClusterNames)) {
          continue;
        } else {
          worker.setProduct(product);
          worker.setCluster(notBlankClusterNames.get(0));
          break;
        }
      }
    }

    if (worker.getProduct() == null) {
      throw new LoginException(ResponseCodeConstant.USER_NO_PRODUCT, "用户未加入项目");
    }

    if (worker.getCluster() == null) {
      throw new LoginException(ResponseCodeConstant.PRODUCT_NO_CLUSTER, "用户加入项目组不存在集群");
    }

    // 添加项目和集群信息到上下文，变更集群项目组集群要更新时上下文
    Integer productId = userService.getProductId(worker.getEmail(), worker.getProduct());
    String clusterId = userService.getClusterId(worker.getEmail(), worker.getProduct(), worker.getCluster());
    worker.setProductId(productId);
    worker.setClusterId(clusterId);
    // 添加session
    sessionHandler.addSession(token, worker, CommonConstants.RedisExpire.EXPIRE_1_DAY);
  }

  /**
   * 清理token
   *
   * @param token token
   */
  public void clearToken(String token) {
    sessionHandler.deleteSession(token);
  }

}

