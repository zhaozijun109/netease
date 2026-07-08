package com.netease.lofter.tango.impl.web.query.trade.activity;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 内容付费-进度类型任务
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class PaidContentActivityProgressTaskQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键id
    */
    private Long id;
    /**
    * 任务名称
    */
    private String name;
    /**
    * 活动id
    */
    private Long actId;
    /**
    * 任务配图
    */
    private String img;
    /**
    * 任务目标
    */
    private Integer goal;
    /**
    * 状态 0:正常; -1:停止（处理一些突发情况）
    */
    private Integer status;
    /**
    * 任务事件类型
    */
    private Integer eventType;
    private Long createTimeBegin;
    private Long createTimeEnd;
    private Long startTime;
    private Long endTime;
    private Long crowdId;

}