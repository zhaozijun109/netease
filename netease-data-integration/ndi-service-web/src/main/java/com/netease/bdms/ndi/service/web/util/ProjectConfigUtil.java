package com.netease.bdms.ndi.service.web.util;

import com.netease.bdms.ndi.service.web.service.ProjectConfigService;
import com.netease.bdms.ndi.service.web.util.constant.CommonConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @ClassName ProjectConfigUtil
 * @Description 项目配置工具类
 * @Author Min Zhao
 * @Version 1.0
 **/
@Component
public class ProjectConfigUtil {

  @Autowired
  private ProjectConfigService configService;

  @Autowired
  private RedisUtil redisUtil;

  public String get(String key) {
    String configValue = redisUtil.hget(CommonConstants.RedisKey.PROJECT_CONFIG_KEY, key);
    if (StringUtils.isNotBlank(configValue)) {
      return configValue;
    } else {
      configValue = configService.getConfig(key);
      redisUtil.hsetWithExpire(CommonConstants.RedisKey.PROJECT_CONFIG_KEY, key, configValue, CommonConstants.RedisExpire.EXPIRE_1_WEEK);
    }

    return configValue;
  }
}
