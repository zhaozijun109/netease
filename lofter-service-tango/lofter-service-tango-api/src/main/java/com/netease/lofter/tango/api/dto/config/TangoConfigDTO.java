package com.netease.lofter.tango.api.dto.config;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class TangoConfigDTO implements Serializable {

    private static final long serialVersionUID = -54983732425938894L;
    /**
     * 应用ID
     */
    private String appId;

    /**
     * 配置key
     */
    private String configKey;

    /**
     * 配置value
     */
    private String configValue;

}
