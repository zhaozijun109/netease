package com.netease.lofter.tango.impl.web.vo;

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
public class OperatorLogVO implements Serializable {
    private static final long serialVersionUID = 8207177782912584096L;
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
     * Controller
     */
    private String controller;
    /**
     * 方法
     */
    private String method;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 参数
     */
    private String params;
    /**
     * 路径
     */
    private String url;
    /**
     * 响应
     */
    private String response;
}