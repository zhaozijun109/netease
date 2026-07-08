package com.netease.lofter.tango.impl.web.vo.trade.activity;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 内容付费-抽奖奖池code
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class PaidContentActivitySlotBooxVO implements Serializable {
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
     * 中奖人数
     */
    private Integer bingoCount;

    /**
     * 虚假的中奖人数
     */
    private Integer fakeBingoCount;

    /**
     * 参与人数
     */
    private Integer joinCount;

    /**
     * 虚假的参与人数
     */
    private Integer fakeJoinCount;

    /**
     * 虚假人数系数下限
     */
    private Integer fakeJoinCountRateMin;

    /**
     * 虚假人数系数上限
     */
    private Integer fakeJoinCountRateMax;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 初始默认摇奖次数
     */
    private Integer defaultInitChanceCount;

    private String activityId;

}