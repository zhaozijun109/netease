package com.netease.bdms.ndi.service.web.util;


import com.alibaba.fastjson.JSONObject;
import com.netease.bdms.ndi.service.web.controller.interceptor.Worker;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HttpsURLConnection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class OpenIDHelper {

  private static final RedisUtil redisUtil = RedisUtil.getRedisUtil();

  private static String openid_server = "https://login.netease.com/openid/";

  private static String MaptoString_url_utf8(Map<String, String> map) {
    String arguments = "?";
    Iterator iter = map.entrySet().iterator();
    boolean first_arg = true;
    while (iter.hasNext()) {
      Map.Entry entry = (Map.Entry) iter.next();
      Object key = entry.getKey();
      Object val = entry.getValue();
      String key_str = (String) key;
      String val_str = (String) val;

      try {
        key_str = URLEncoder.encode(key_str, "UTF-8");
        val_str = URLEncoder.encode(val_str, "UTF-8");
      } catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }

      if (first_arg == false) {
        arguments = arguments + "&";
      }
      first_arg = false;

      arguments = arguments + key_str;
      arguments = arguments + "=";
      arguments = arguments + val_str;
    }
    return arguments;
  }

  public static String getRedirectURL(String host, HttpSession session, String redirect) {
    HashMap<String, String> assoc_data = new HashMap<String, String>();
    assoc_data.put("openid.mode", "associate");
    assoc_data.put("openid.assoc_type", "HMAC-SHA256");
    assoc_data.put("openid.session_type", "no-encryption");

    String arguments = MaptoString_url_utf8(assoc_data);
    String assoc_handle = null;

    try {
      URL url = new URL(openid_server + arguments);
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
      BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
      String str = "";
      do {
        try {
          str = r.readLine();
          if (str == null) {
            break;
          }

          String[] temp_arrays = str.split(":");
          if (temp_arrays[0].equals("assoc_handle")) {
            assoc_handle = temp_arrays[1];
            redisUtil.set("assoc_handle", assoc_handle);
//						session.setAttribute("assoc_handle", assoc_handle);
          } else if (temp_arrays[0].equals("mac_key")) {
            String mac_key = temp_arrays[1];
            redisUtil.set("mac_key", mac_key);
//						session.setAttribute("mac_key", mac_key);
          }

        } catch (IOException e) {
          e.printStackTrace();
        }

      } while (str != null);

      Map<String, String> redirect_data = new HashMap<String, String>();
      redirect_data.put("openid.ns", "http://specs.openid.net/auth/2.0");
      redirect_data.put("openid.mode", "checkid_setup");
      redirect_data.put("openid.assoc_handle", assoc_handle);
      redirect_data.put("openid.return_to", host + "api/logon" + (redirect == null ? "" : ("?redirect=" + URLEncoder.encode(redirect, "UTF-8"))));
      redirect_data.put("openid.claimed_id", "http://specs.openid.net/auth/2.0/identifier_select");
      redirect_data.put("openid.identity", "http://specs.openid.net/auth/2.0/identifier_select");
      redirect_data.put("openid.realm", host);
      redirect_data.put("openid.ns.sreg", "http://openid.net/extensions/sreg/1.1");
      redirect_data.put("openid.sreg.required", "nickname,email,fullname");
      redirect_data.put("openid.ns.ax", "http://openid.net/srv/ax/1.0");
      redirect_data.put("openid.ax.mode", "fetch_request");
      redirect_data.put("openid.ax.type.empno", "https://login.netease.com/openid/empno/");
      redirect_data.put("openid.ax.type.dep", "https://login.netease.com/openid/dep/");
      redirect_data.put("openid.ax.required", "empno,dep");

      arguments = MaptoString_url_utf8(redirect_data);
      return openid_server + arguments;

    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    } catch (Exception e1) {
      e1.printStackTrace();
    }

    return "index.html";
  }

  /**
   * @param
   * @param request
   * @return
   */
  public static Worker checkAuth(HttpServletRequest request) {
    HttpSession session = request.getSession();
    try {

      HashMap<String, String> auth_response = new HashMap<String, String>();
      Map<String, String[]> params = request.getParameterMap();

      for (String key : params.keySet()) {
        auth_response.put(key, request.getParameter(key));
      }

      if (auth_response.get("openid.mode").equals("id_res") == false) {
        System.out.println("openid.mode 返回值不是 id_res, 认证失败!");
        return null;
      }


      if (auth_response.get("openid.assoc_handle").equals(redisUtil.get("assoc_handle")) == false) {
        System.out.println("assoc_handle 不一致，使用check authentication！");
        if (!check_authentication(auth_response)) {
          return null;
        }
      }
      String[] signed_items = auth_response.get("openid.signed").split(",");
      String signed_content = "";
      for (int i = 0; i < signed_items.length; i++) {
        signed_content = signed_content + signed_items[i];
        signed_content = signed_content + ":";
        signed_content = signed_content + auth_response.get("openid." + signed_items[i]);
        signed_content = signed_content + "\n";
      }

      byte[] decoded64 = (new sun.misc.BASE64Decoder()).decodeBuffer(redisUtil.get("mac_key"));
      SecretKey signingKey = new SecretKeySpec(decoded64, "HMACSHA256");
      Mac mac = null;
      try {
        mac = Mac.getInstance("HMACSHA256");
      } catch (NoSuchAlgorithmException e) {
        e.printStackTrace();
      }
      try {
        mac.init(signingKey);
      } catch (InvalidKeyException e) {
        e.printStackTrace();
      }
      byte[] digest = mac.doFinal(signed_content.getBytes("UTF-8"));
      String signature = (new sun.misc.BASE64Encoder()).encode(digest);
      System.out.println("openid server返回的签名是:" + auth_response.get("openid.sig"));
      System.out.println("consumer 计算出来的签名是:" + signature);
      if (signature.equals(auth_response.get("openid.sig")) == true) {
        System.out.println("签名一致，验证成功！");
      } else {
        System.out.println("签名不一致，验证失败！");
        return null;
      }

      redisUtil.del("assoc_handle");
      redisUtil.del("mac_key");

      return new Worker(auth_response.get("openid.sreg.fullname"), auth_response.get("openid.sreg.email"));
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

  private static boolean check_authentication(HashMap<String, String> auth_response) {
    /*将openid.mode参数的值设置为check_authentication，其他参数和值不变，发回给OpenID server*/
    auth_response.put("openid.mode", "check_authentication");

    String arguments = MaptoString_url_utf8(auth_response);
    try {
      URL url = new URL(openid_server + arguments);

      System.out.println(url);
      HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
      BufferedReader r = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
      String str = "";
      String auth_result = "";

      do {
        try {
          str = r.readLine();

          if (str == null) {
            break;
          }
          String[] temp_arrays = str.split(":");
          if (temp_arrays[0].equals("is_valid")) {
            auth_result = temp_arrays[1];
          }

        } catch (IOException e) {
          e.printStackTrace();
        }

      } while (str != null);

      if (auth_result.equals("true")) {
        System.out.println("check authentication 认证成功！");
        return true;
      } else {
        System.out.println("check authentication 认证失败！");
      }

    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e1) {
      e1.printStackTrace();
    } catch (IOException e1) {
      e1.printStackTrace();
    }

    return false;
  }

  public static void loginfake(HttpServletRequest request, HttpServletResponse response) throws Exception {
    Worker worker = new Worker("赵敏","zhaomin3@corp.netease.com");
    worker.setProduct("intern");
    worker.setCluster("滨江");
    String sessionId = request.getSession().getId();
    CookieUtil.add(response, CommonConstants.REDIS_SESSION_ID, sessionId);
    redisUtil.set(sessionId, JSONObject.toJSONString(worker));
    CookieUtil.setCookies(response, worker.getUsername(), worker.getEmail(), worker.getProduct(), worker.getCluster());
  }
}
