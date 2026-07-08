package com.netease.bdms.ndi.service.web.util.constant;

/**
 * @ClassName ResponseCodeConstant
 * @Description 响应状态码常量
 * @Author Min Zhao
 * @Version 1.0
 **/
public class ResponseCodeConstant {

  /**
   * 成功
   */
  public static final int SUCCESS = 200;

  /**
   * 服务异常
   */
  public static final int SERVER_ERROR = -1;

  /**
   * 依赖服务异常
   */
  public static final int OTHER_SERVER_ERROR = -2;

  /**
   * 参数异常
   */
  public static final int PARAM_INVALID = 20001;

  /**
   * Azkaban请求错误
   */
  public static final int AZKABAN_REQUEST_ERROR = 3002;

  /**
   * Azkaban响应错误
   */
  public static final int AZKABAN_RESPONSE_ERROR = 3003;

  public static final int AZKABAN_SERVER_ERROR = 3001;

  /**
   * 数据源被引用
   */
  public static final int DATA_SOURCE_QUOTED = 1001;

  /**
   * 数据源url非法
   */
  public static final int DATA_SOURCE_URL_ILLEGAL = 1002;

  /**
   * 数据源名字已存在
   */
  public static final int DATA_SOURCE_NAME_EXIST = 1003;

  /**
   * 数据源已存在
   * url + user
   */
  public static final int DATA_SOURCE_EXIST = 1004;

  /**
   * 数据源不存在
   */
  public static final int DATA_SOURCE_NO_EXIST = 1005;

  public static final int TASK_QUOTED = 2001;

  public static final int TASK_NAME_EXIST = 2002;

  /**
   * 任务不存在
   */
  public static final int TASK_NO_EXIST = 2003;

  /**
   * 用户没有符合添加的项目
   */
  public static final int USER_NO_PRODUCT = 3001;

  /**
   * 用户项目没有集群
   */
  public static final int PRODUCT_NO_CLUSTER = 3002;

  public static final int PRODUCT_NO_GROUP = 3003;




}
