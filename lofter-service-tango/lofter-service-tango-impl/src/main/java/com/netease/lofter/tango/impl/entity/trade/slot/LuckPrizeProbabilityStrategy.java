package com.netease.lofter.tango.impl.entity.trade.slot;

import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

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
@FieldNameConstants
@Table("`Luck_PrizeProbabilityStrategy`")
public class LuckPrizeProbabilityStrategy implements Serializable {
    /**
     * ID
     */
    @Key
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
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 奖品中奖策略信息,
     */
    private String probabilityStrategy;

    private static final long serialVersionUID = -5656661919033596858L;
}