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
 * <p>@Create Time: 2024-07-23 10:20:37</p>
 * 此实体PO对应表:  Luck_PrizeProbabilityStrategy
 */
@Getter
@Setter
public class LuckPrizeProbabilityStrategyQuery extends BaseQuery implements Serializable {
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
     * 开始时间
     */
    private Long startTime;

    /**
     * 结束时间
     */
    private Long endTime;

    /**
     * 奖品中奖策略信息,
     */
    private String probabilityStrategy;

    private Long createTimeBegin;

    private Long createTimeEnd;

    private static final long serialVersionUID = -7145461704279778993L;
}