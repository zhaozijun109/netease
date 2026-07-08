package com.netease.bdms.ndi.service.web.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Preconditions;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.param.User;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * @ClassName ParamsValidateUtil
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public class ParamUtil {
  private static final Logger log = LoggerFactory.getLogger(ParamUtil.class);

  /**
   * @param params
   */
  public static void validate(Object... params) {
    for (Object param : params) {
      Preconditions.checkArgument(param != null);
    }
  }

  public static void nonNull(Object param, String message) {
    Preconditions.checkArgument(param != null, message);
  }

  public static void userCheck(HttpServletRequest request, String email, String product) {
    validate(email, product);
    String cookieEmail = CookieUtil.get(request, CommonConstants.CookieKey.USER_EMAIL);
    if (!StringUtils.equalsIgnoreCase(cookieEmail.trim(), email.trim())) {
      throw new IllegalArgumentException("");
    }
    String cookieProduct = CookieUtil.get(request, CommonConstants.CookieKey.USER_PRODUCT);
    if (!StringUtils.equalsIgnoreCase(cookieProduct.trim(), product.trim())) {
      throw new IllegalArgumentException("");
    }
  }

  public static User parseUser(JSONObject param){
    ParamUtil.nonNull(param, "User can't be null");
    JSONObject userJSON = param.getJSONObject("user");
    User user = JSON.parseObject(userJSON.toString(), User.class);
    if (user == null || StringUtils.isAnyBlank(user.getEmail(), user.getProduct(), user.getCluster())){
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "The email, product and cluster can't be blank");
    }
    return user;
  }

  public static void main(String[] args) {
    String str = null;
    try {
      str = null;
      System.out.println("str:" + str.getBytes());
    } catch (Exception e) {
      System.out.println(e.getMessage());
//            e.printStackTrace();
    }
    System.out.println("TEST");
  }


}
