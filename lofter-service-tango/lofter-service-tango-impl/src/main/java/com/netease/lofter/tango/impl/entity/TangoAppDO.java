package com.netease.lofter.tango.impl.entity;

import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.sql.Timestamp;

@FieldNameConstants
@Table("TangoApp")
@Getter
@Setter
public class TangoAppDO implements Serializable {

    private static final long serialVersionUID = 5636847050114434817L;
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
     * 应用名称
     */
    private String appName;

    /**
     * 最后操作人
     */
    private String operator;
}
