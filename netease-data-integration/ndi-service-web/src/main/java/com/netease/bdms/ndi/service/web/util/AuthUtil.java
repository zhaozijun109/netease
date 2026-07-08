package com.netease.bdms.ndi.service.web.util;

import com.netease.bdms.ndi.service.web.service.ServiceInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @ClassName: AuthUtil
 * @Author:
 * @Date: 2018/9/7 15:12
 * @Description: 权限认证工具类
 * @Version: 1.0
 */
@Component
public class AuthUtil {
  private static final Logger log = LoggerFactory.getLogger(AuthUtil.class);
  @Autowired
  private ServiceInfo serviceInfo;

  /**
   * 生成签名
   *
   * @param secret
   * @param timestamp
   * @return
   */
  public String generateSignature(String secret, long timestamp) {
    BASE64Encoder base64Encoder = new BASE64Encoder();
    MessageDigest messageDigest = null;
    try {
      messageDigest = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      log.error("_" + e.getMessage(), e);
    }
    return base64Encoder.encode(messageDigest.digest((secret + timestamp).getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * 校验接口签名
   *
   * @param request
   * @return
   */
  public Boolean checkSignature(HttpServletRequest request){
    String serviceName = request.getHeader("appId");
    String secret = serviceInfo.getServiceSecret(serviceName);
    String signature = request.getHeader("appSecret");
    return StringUtils.equalsIgnoreCase(secret, signature);
  }

  public static void main(String[] args) {
    AuthUtil authUtil = new AuthUtil();
    String token = authUtil.generateSignature("ndi_client", System.currentTimeMillis());
    System.out.println(token);
  }
}
