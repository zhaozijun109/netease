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
 * <p>@Create Time: 2024-06-05 20:34:55</p>
 * 此实体PO对应表:  Tango_Operator_Log
 */
@Getter
@Setter
@FieldNameConstants
@Table("`Tango_Operator_Log`")
public class OperatorLog implements Serializable {
    private static final long serialVersionUID = 2492419418198980019L;
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
    /**
     * 路径
     */
    private String url;
    /**
     * 响应
     */
    private String response;
}