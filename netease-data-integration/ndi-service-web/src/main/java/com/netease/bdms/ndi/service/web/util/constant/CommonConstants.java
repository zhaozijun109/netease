package com.netease.bdms.ndi.service.web.util.constant;

/**
 * @ClassName CommonConstants
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface CommonConstants {

  /**
   * 时间常量
   */
  class Time {

    /**
     * 1秒，单位毫秒
     */
    public static final long MS_1_SECOND = 1000L;

    /**
     * 1分钟，单位毫秒
     */
    public static final long MS_1_MINUTE = 60 * MS_1_SECOND;

    /**
     * 1小时，单位毫秒
     */
    public static final long MS_1_HOUR = 60 * MS_1_MINUTE;

    /**
     * 1天，单位毫秒
     */
    public static final long MS_1_DAY = 24 * MS_1_HOUR;

    /**
     * 1秒，单位秒
     */
    public static final int S_1_SECOND = 1;

    /**
     * 1分钟，单位秒
     */
    public static final int S_1_MINUTE = 60 * S_1_SECOND;

    /**
     * 1小时，单位秒
     */
    public static final int S_1_HOUR = 60 * S_1_MINUTE;

    /**
     * 1天，单位秒
     */
    public static final int S_1_DAY = 24 * S_1_HOUR;

  }

  /**
   * token相关常量
   */
  class Token {

    /**
     * token名
     */
    public static final String NAME = "ndi_session_id";

    /**
     * token有效毫秒数
     */
    public static final long VALID_MS = Time.MS_1_DAY;

    /**
     * token有效秒数
     */
    public static final int VALID_S = Time.S_1_DAY;

  }

  String NDI_SERVICE_NAME = "NDI";


  String MAMMUT_SERVICE_NAME = "NDI";

  //seconds
  int REDIS_CATALOG_EXPIRE = 60 * 30;

  String REDIS_USER_PRODUCT = "product";

  String REDIS_USER_CLUSTER = "cluster";

  String REDIS_USER_PRODUCT_ID = "productId";

  String REDIS_USER_CLUSTER_ID = "clusterId";

  String REDIS_PRODUCT_USER = "NDI_PRODUCT_USER";

  String REDIS_USER_INFO = "NDI_USER_INFO";

  String REDIS_SESSION_ID = "ndi_session_id";

  int REDIS_SESSION_EXPIRE = 60 * 60 * 24;

  String COOKIE_USER_NAME = "username";

  String COOKIE_EMAIL = "email";

  String COOKIE_PRODUCT = "product";

  String COOKIE_CLUSTER = "cluster";

  String TASK_TYPE_DEVELOP = "develop";

  String TASK_TYPE_ONLINE = "online";


  interface Session {
    String SESSION_EXPIRE = "session.expire.time.s";
  }


  interface RedisKey {
    String CATALOG_CACHE_KEY = "ndi_catalog_cache";

    String PROJECT_CONFIG_KEY = "ndi_project_config_key";
  }

  interface RedisExpire {
    // s，一天
    int EXPIRE_1_DAY = 60 * 60 * 24;

    int EXPIRE_1_WEEK = 7 * EXPIRE_1_DAY;

    int EXPIRE_1_MONTH = 4 * EXPIRE_1_WEEK;
  }

  interface CookieKey {
    String USER_EMAIL = "email";
    String USER_PRODUCT = "product";
  }

  interface MetahubServcie {
    String ADDRESS = "meta.server.address";
    String APPID = "metahub.appid";
    String SECRET = "metahub.secret";
  }

  interface MammutService {
    String ADDRESS = "mammut.service.address";
    String API_KEY = "mammut.api.key";
    String MASTER_KEY = "mammut.master.key";
  }

  interface AacService {
    String SERVER_URI = "aac.serverUri";
    String APP_INDEX_URI = "aac.appIndexUri";
    String APP_CLEAR_URI = "aac.appClearUri";
    String PATH_LOGIN = "aac.path.login";
    String PATH_LOGOUT = "aac.path.logout";
    String PATH_GET_USER = "aac.path.getUser";
    String PATH_CLEAR_TOKEN = "aac.path.clearToken";
  }

  interface RedisConfig {
    String IP = "redis.ip";
    String PORT = "redis.port";
    String PASSWORD = "redis.password";
    String MAX_TOTAL = "redis.max.total";
    String MAX_IDLE = "redis.max.idle";
    String MIN_IDLE = "redis.min.idle";
  }

  interface AzkabanConfig {
    String AZKABAN_USER_NAME = "azkaban.user.name";
    String AZKABAN_PASSWORD = "azkaban.password";
  }

  interface WhiteList{
    String IP = "ip.white.list";
    String API = "api.white.list";
  }

  interface HTTP_STATUS_CODE {
    Integer SUCCESS = 200;
  }
}
