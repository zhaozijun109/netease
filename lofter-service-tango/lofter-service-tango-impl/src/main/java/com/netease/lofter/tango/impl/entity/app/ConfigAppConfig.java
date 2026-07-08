package com.netease.lofter.tango.impl.entity.app;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;

/**
* App全局静态配置
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Config_AppConfig")
public class ConfigAppConfig implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 描述
    */
    @Column(name="description")
    private String description;
    /**
    * 平台
    */
    @Column(name="platform")
    private Integer platform;
    /**
    * 开始版本号
    */
    @Column(name="startVersion")
    private String startVersion;
    /**
    * 结束版本号
    */
    @Column(name="endVersion")
    private String endVersion;
    /**
    * key
    */
    @Column(name="configKey")
    private String configKey;
    /**
    * 配置
    */
    @Column(name="configValue")
    private String configValue;
    /**
    * 状态 0-正常，-1删除
    */
    @Column(name="status")
    private Integer status;
    /**
    * 创建时间
    */
    @Column(name="createTime")
    private Long createTime;
}