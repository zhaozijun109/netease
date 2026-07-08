package com.netease.lofter.tango.api.client.config;

import com.netease.lofter.tango.api.dto.config.TangoConfigDTO;

import java.util.List;

public interface TangoConfigRpcService {

    List<TangoConfigDTO> listByAppId(String env, String appId);

    TangoConfigDTO getByAppIdAndKey(String env, String appId, String key);
}
