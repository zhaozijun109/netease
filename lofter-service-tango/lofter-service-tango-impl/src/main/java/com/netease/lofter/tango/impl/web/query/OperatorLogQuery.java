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
 * <p>@Create Time: 2024-06-05 20:34:55</p>
 * 此实体PO对应表:  Tango_Operator_Log
 */
@Getter
@Setter
public class OperatorLogQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = 6672019530914269901L;
    /**
     * 主键
     */
    private Long id;
    /**
     * Controller
     */
    private String controller;
    /**
     * 方法类名.方法名
     */
    private String method;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 参数请求信息
     */
    private String params;
    private Long createTimeBegin;

    private Long createTimeEnd;
}