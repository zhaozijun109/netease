package com.netease.lofter.tango.impl.web.vo;

import lombok.Getter;
import lombok.Setter;

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
public class TangoConfigOpHistoryVO implements Serializable {
    private static final long serialVersionUID = -2729107285290056289L;
    /**
     * 主键
     */
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

    private String envTags;

}