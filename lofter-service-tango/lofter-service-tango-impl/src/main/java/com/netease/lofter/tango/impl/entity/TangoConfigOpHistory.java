package com.netease.lofter.tango.impl.entity;

import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * <p>Title:  </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-06-13 18:10:40</p>
 * 此实体PO对应表:  tango_config_op_history
 */
@Getter
@Setter
@FieldNameConstants
@Table("`tango_config_op_history`")
public class TangoConfigOpHistory implements Serializable {
    private static final long serialVersionUID = -3009640014283823419L;
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
     * 修改时间
     */
    private Long updateTime;
    /**
     * 应用Id
     */
    private String appId;
    /**
     * key
     */
    private String configKey;
    /**
     * 最后操作人
     */
    private String operator;
    /**
     * 操作类型
     */
    private String opType;
    /**
     * 新值
     */
    private String newValue;
    /**
     * 旧值
     */
    private String oldValue;

    /**
     * 环境标签
     */
    private String envTags;
}