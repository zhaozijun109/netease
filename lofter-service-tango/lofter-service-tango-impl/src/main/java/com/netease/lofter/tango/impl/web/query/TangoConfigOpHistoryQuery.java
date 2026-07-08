package com.netease.lofter.tango.impl.web.query;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
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
public class TangoConfigOpHistoryQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = 2291758457789322256L;
    /**
     * 主键
     */
    private Long id;
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
    private Long createTimeBegin;
    private Long createTimeEnd;
}