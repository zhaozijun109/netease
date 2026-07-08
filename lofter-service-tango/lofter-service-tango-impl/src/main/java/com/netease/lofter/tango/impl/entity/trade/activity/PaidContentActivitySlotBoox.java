package com.netease.lofter.tango.impl.entity.trade.activity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.SqlIgnore;

/**
* 内容付费-抽奖奖池code
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("PaidContent_ActivitySlotBoox")
public class PaidContentActivitySlotBoox implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键id
    */
    @Key
    @Column(name="id",value = "seq")
    private Long id;
    /**
    * 活动
    */
    @Column(name="name")
    private String name;
    /**
    * 活动id
    */
    @Column(name="actId")
    @SqlIgnore(selectIgnore = false, insertIgnore = false)
    private Long actId;
    /**
    * 奖池code
    */
    @Column(name="lootboxCode")
    private String lootboxCode;
    /**
    * 活动开始时间
    */
    @Column(name="startTime")
    private Long startTime;
    /**
    * 活动结束时间
    */
    @Column(name="endTime")
    private Long endTime;
    /**
    * 活动结束时间
    */
    @Column(name="createTime")
    private Long createTime;
}