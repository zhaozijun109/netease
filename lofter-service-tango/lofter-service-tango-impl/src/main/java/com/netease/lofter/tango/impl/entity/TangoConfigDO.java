package com.netease.lofter.tango.impl.entity;

import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.sql.Timestamp;

@FieldNameConstants
@Table("TangoConfig")
@Getter
@Setter
public class TangoConfigDO implements Serializable {

    private static final long serialVersionUID = 3139130789402215644L;

    /**
     * 主键
     */
    @Key
    private Long id;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 数据库更新时间
     */
    private Timestamp dbUpdateTime;

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

    /**
     * 配置元数据
     */
    private String configMeta;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 最后操作人
     */
    private String operator;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 允许修改的用户列表
     */
    private String userlist;

    /**
     * 环境标签
     */
    private String envTags;

}
