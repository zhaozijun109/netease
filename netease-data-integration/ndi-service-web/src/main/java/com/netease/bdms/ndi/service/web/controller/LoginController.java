package com.netease.bdms.ndi.service.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.config.ContextConstant;
import com.netease.bdms.ndi.service.web.config.NdiContext;
import com.netease.bdms.ndi.service.web.controller.interceptor.SessionHandler;
import com.netease.bdms.ndi.service.web.controller.interceptor.Worker;
import com.netease.bdms.ndi.service.web.dto.AccountDto;
import com.netease.bdms.ndi.service.web.dto.CheckLoginDto;
import com.netease.bdms.ndi.service.web.exception.LoginException;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.service.UserService;
import com.netease.bdms.ndi.service.web.service.impl.MammutMetaService;
import com.netease.bdms.ndi.service.web.util.*;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import com.netease.bdms.ndi.service.web.util.constant.ResponseCodeConstant;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping
public class LoginController {
  private static final Logger log = LoggerFactory.getLogger(LoginController.class);
  @Autowired
  private MammutMetaService mammutMetaService;

  @Autowired
  private RedisUtil redisUtil;

  @Autowired
  private SessionHandler sessionHandler;

  @Autowired
  private UserService userService;

  @RequestMapping("/api/login")
  public void login(HttpServletRequest request, HttpServletResponse response) throws Exception {
    String serverName = request.getServerName();
    log.info("server name is " + serverName);
    String referer = request.getHeader("referer");
    log.info("referer is " + referer);

    // Login fake, for frontend to test
    if ((referer != null && referer.startsWith("http://localhost")) || StringUtils.equals(serverName, "localhost")) {
      OpenIDHelper.loginfake(request, response);
      response.sendRedirect(referer);
      return;
    }

    String redirect = OpenIDHelper.getRedirectURL(
      request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/",
      request.getSession(), "/api/logon");
    response.sendRedirect(redirect);
  }

  @RequestMapping("/api/logon")
  public void logon(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Worker worker = OpenIDHelper.checkAuth(request);
    if (worker == null) {
      log.error("worker is null, please login again");
      return;
    }

    JSONObject accounts = mammutMetaService.getAccount(worker.getEmail());
    List<String> products = accounts.getJSONArray("result").toJavaList(String.class);
    List<String> notBlankProducts = products.stream()
            .filter(item -> StringUtils.isNotBlank(item)).collect(Collectors.toList());

    if (notBlankProducts == null || notBlankProducts.size() == 0) {
      log.error("No product can be found for user, user email: " + worker.getEmail());
      response.sendRedirect("/error/no-product");
      return;
    }

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

    if (StringUtils.isNotBlank(lastProduct) && StringUtils.isNotBlank(lastCluster)){
      List<String> clusterNames = mammutMetaService.getClusterName(lastProduct);
      List<String> notBlankClusterNames = clusterNames.stream()
              .filter(item -> StringUtils.isNotBlank(item))
              .collect(Collectors.toList());
      if (notBlankClusterNames != null && notBlankClusterNames.size() != 0 && notBlankClusterNames.contains(lastCluster)){
        worker.setProduct(lastProduct);
        worker.setCluster(lastCluster);
      }
    }
    if (worker.getProduct() == null && worker.getCluster() == null){
      for (String product: notBlankProducts){
        List<String> clusterNames = mammutMetaService.getClusterName(product);
        List<String> notBlankClusterNames = clusterNames.stream()
                .filter(item -> StringUtils.isNotBlank(item))
                .collect(Collectors.toList());
        if (notBlankClusterNames == null || notBlankClusterNames.size() == 0){
          continue;
        } else {
          worker.setProduct(product);
          worker.setCluster(notBlankClusterNames.get(0));
          break;
        }
      }
      if (worker.getProduct() == null || worker.getCluster() == null){
        log.warn("All product can't find cluster, user email: {}", worker.getEmail());
        response.sendRedirect("/error/no-cluster");
        return;
      }
    }

    String sessionId = sessionHandler.generateSessionId(request);
    sessionHandler.addSession(response, sessionId, worker, CommonConstants.REDIS_SESSION_EXPIRE);
    CookieUtil.setCookies(response, worker.getUsername(), worker.getEmail(), worker.getProduct(), worker.getCluster());

    response.setHeader("Cache-Control", "no-cache");
    response.sendRedirect("/");
  }

  @GetMapping(value = "/api/check/login")
  @ResponseBody
  public ResponseResult<CheckLoginDto> checkLogin(@RequestParam(value = "product", required = false) String product,
                                                  @RequestParam(value = "clusterId", required = false) String clusterId,
                                                  HttpServletRequest request, HttpServletResponse response) throws IOException {

    String sessionId = CookieUtil.get(request, CommonConstants.REDIS_SESSION_ID);
    if (StringUtils.isBlank(sessionId)) {
      return ResponseResult.createBySuccess(new CheckLoginDto(false, null, null));
    }

    String workerStr = (String) sessionHandler.getSession(sessionId);
    if (StringUtils.isBlank(workerStr)) {
      return ResponseResult.createBySuccess(new CheckLoginDto(false, null, null));
    }

    Worker worker = JSONObject.parseObject(workerStr, Worker.class);
    if (worker == null) {
      return ResponseResult.createBySuccess(new CheckLoginDto(false, null, null));
    }

    if (StringUtils.isNotBlank(product) && StringUtils.isNotBlank(clusterId)) {
//      List<String> userProductList = userService.listUserProduct(worker.getEmail());
//
//      if (!userProductList.contains(product)) {
//        throw new LoginException(ResponseCodeConstant.USER_NO_PRODUCT, "用户未加入项目，请先加入项目");
//      }

      String cluster = null;
      String email = NdiContext.get(ContextConstant.EMAIL);
      AccountDto accountDto = userService.getAccountDtoByEmail(email);
      if (accountDto == null) {
        throw new LoginException(ResponseCodeConstant.PRODUCT_NO_GROUP, "项目未加入项目组，请先加入项目组");
      }

      List<AccountDto.ProductDto> productDtoList = accountDto.getProductDtoList();
      List<String> productList = productDtoList.stream().map(item -> item.getProduct()).collect(Collectors.toList());
      if (!productList.contains(product)) {
        throw new LoginException(ResponseCodeConstant.PRODUCT_NO_GROUP, "项目未加入项目组，请先加入项目组");
      }

      for (AccountDto.ProductDto productDto : productDtoList) {
        if (StringUtils.equalsIgnoreCase(productDto.getProduct(), product)) {
          List<AccountDto.ClusterDto> clusterDtoList = productDto.getClusterDtoList();
          for (AccountDto.ClusterDto clusterDto : clusterDtoList) {
            if (StringUtils.equalsIgnoreCase(clusterId, clusterDto.getClusterId())) {
              cluster = clusterDto.getCluster();
            }
          }
        }
      }
      if (StringUtils.isBlank(cluster)) {
        throw new LoginException(ResponseCodeConstant.PRODUCT_NO_CLUSTER, "该项目不存在集群");
      }
      worker = userService.switchProductAndCluster(sessionId, email, product, cluster);
    }
    CheckLoginDto checkLoginDto = new CheckLoginDto(true, worker, sessionId);
    return ResponseResult.createBySuccess(checkLoginDto);
  }

  @RequestMapping("/api/logout")
  @ResponseBody
  public ResponseResult logout(HttpServletRequest request, HttpServletResponse response) {
    String sessionId = CookieUtil.get(request, CommonConstants.REDIS_SESSION_ID);
    sessionHandler.deleteSession(sessionId);
    CookieUtil.remove(request, response, CommonConstants.COOKIE_USER_NAME, CommonConstants.COOKIE_EMAIL,
      CommonConstants.COOKIE_PRODUCT, CommonConstants.COOKIE_CLUSTER);
    return ResponseResult.createBySuccess();
  }

  @GetMapping(value = "/api/login/test")
  @ResponseBody
  public ResponseResult test() {
    return ResponseResult.createBySuccess("TEST");
  }
}
