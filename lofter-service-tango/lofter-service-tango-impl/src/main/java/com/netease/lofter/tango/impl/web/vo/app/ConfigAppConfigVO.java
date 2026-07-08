package com.netease.lofter.tango.impl.web.vo.app;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* App全局静态配置
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class ConfigAppConfigVO implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    private Long id;
    /**
    * 描述
    */
    private String description;
    /**
    * 平台
    */
    private Integer platform;
    /**
    * 开始版本号
    */
    private String startVersion;
    /**
    * 结束版本号
    */
    private String endVersion;
    /**
    * key
    */
    private String configKey;
    /**
    * 配置
    */
    private String configValue;
    /**
    * 状态 0-正常，-1删除
    */
    private Integer status;
    /**
    * 创建时间
    */
    private Long createTime;
}