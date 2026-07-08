package com.netease.lofter.tango.impl.web.query.trade.activity;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 内容付费-抽奖奖池code
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class PaidContentActivitySlotBooxQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键id
    */
    private Long id;
    /**
    * 活动
    */
    private String name;
    /**
    * 活动id
    */
    private Long actId;
    /**
    * 奖池code
    */
    private String lootboxCode;
    /**
    * 活动开始时间
    */
    private Long startTime;
    /**
    * 活动结束时间
    */
    private Long endTime;
    /**
    * 活动结束时间
    */
    private Long createTime;
    private Long createTimeBegin;
    private Long createTimeEnd;

    /**
     * 单日摇奖初始次数
     */
    private Integer dailyInitChanceCount;

    /**
     * 每天抽奖次数限制
     */
    private Integer dailySlotLimit;

    /**
     * 单个用户可中奖数量
     */
    private Integer userBingoLimit;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 初始默认摇奖次数
     */
    private Integer defaultInitChanceCount;

}