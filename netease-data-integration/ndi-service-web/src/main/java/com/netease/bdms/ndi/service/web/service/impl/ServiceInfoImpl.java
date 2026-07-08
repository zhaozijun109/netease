package com.netease.bdms.ndi.service.web.service.impl;

import com.netease.bdms.ndi.service.web.dao.ServiceInfoDOMapper;
import com.netease.bdms.ndi.service.web.exception.NdiException;
import com.netease.bdms.ndi.service.web.service.ServiceInfo;
import com.netease.bdms.ndi.service.web.util.ProcessStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @ClassName ServerInfoImpl
 * @Description ServerInfo
 * @Author Min Zhao
 * @Version 1.0
 **/
@Service
public class ServiceInfoImpl implements ServiceInfo {
  @Autowired
  private ServiceInfoDOMapper serviceInfoDOMapper;

  @Override
  public String getServiceSecret(String serverName) {
    if (StringUtils.isBlank(serverName)) {
      throw new NdiException(ProcessStatusEnum.ILLEGAL_ARGUMENT.getCode(), "AppId不能为空");
    }

    String secret = serviceInfoDOMapper.selectSecretByServerName(serverName);
    return secret;
  }
}
