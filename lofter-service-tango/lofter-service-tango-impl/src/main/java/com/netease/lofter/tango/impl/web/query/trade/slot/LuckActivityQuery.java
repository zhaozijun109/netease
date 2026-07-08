package com.netease.lofter.tango.impl.web.query.trade.slot;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>Title:  </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>@author: jetbi</p>
 * <p>@Create Time: 2024-07-23 10:14:10</p>
 * 此实体PO对应表:  Luck_Activity
 */
@Getter
@Setter
public class LuckActivityQuery extends BaseQuery implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 产品标识
     */
    private String appKey;

    /**
     * 活动标识
     */
    private String activityId;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 活动开始时间，闭
     */
    private Long startTime;

    /**
     * 活动结束时间，开
     */
    private Long endTime;

    /**
     * 状态：-1，无效；0，正常；
     */
    private Byte status;

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
     * 初始默认摇奖次数
     */
    private Integer defaultInitChanceCount;

    private Long createTimeBegin;

    private Long createTimeEnd;

    private static final long serialVersionUID = 5941566021059790164L;
}