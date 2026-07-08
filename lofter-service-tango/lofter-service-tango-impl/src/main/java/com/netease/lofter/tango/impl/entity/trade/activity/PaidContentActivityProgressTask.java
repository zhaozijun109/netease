package com.netease.lofter.tango.impl.entity.trade.activity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;

/**
* 内容付费-进度类型任务
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("PaidContent_ActivityProgressTask")
public class PaidContentActivityProgressTask implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键id
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 任务名称
    */
    @Column(name="name")
    private String name;
    /**
    * 活动id
    */
    @Column(name="actId")
    private Long actId;
    /**
    * 任务配图
    */
    @Column(name="img")
    private String img;
    /**
    * 任务配图
    */
    @Column(name="tip")
    private String tip;
    /**
    * 任务目标
    */
    @Column(name="goal")
    private Integer goal;
    /**
    * 状态 0:正常; -1:停止（处理一些突发情况）
    */
    @Column(name="status")
    private Integer status;
    /**
    * 任务事件类型
    */
    @Column(name="eventType")
    private Integer eventType;
    @Column(name="crowdId")
    private Long crowdId;
    @Column(name="startTime")
    private Long startTime;
    @Column(name = "endTime")
    private Long endTime;
    /**
     * 任务循环类型
     */
    @Column(name="cycleType")
    private Integer cycleType;
}