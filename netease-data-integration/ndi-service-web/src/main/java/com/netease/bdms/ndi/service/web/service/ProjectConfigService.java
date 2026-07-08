package com.netease.bdms.ndi.service.web.service;

/**
 * @ClassName ProjectConfigService
 * @Description TODO
 * @Author Min Zhao
 * @Version 1.0
 **/
public interface ProjectConfigService {
    String getConfig(String key);

    String getConfig(String key, String namespace);

    void setConfig(String key, String value);

    void setConfig(String key, String value, String namespace);

    void updateConfig(String key, String value, String namespace);

}
