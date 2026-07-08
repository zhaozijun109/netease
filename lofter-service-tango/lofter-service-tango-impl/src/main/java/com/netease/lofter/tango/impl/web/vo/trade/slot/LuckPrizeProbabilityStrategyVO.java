package com.netease.lofter.tango.impl.web.vo.trade.slot;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * <p>Title:  </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>@author: shiliang</p>
 * <p>@Create Time: 2024-07-23 10:20:37</p>
 * 此实体PO对应表:  Luck_PrizeProbabilityStrategy
 */
@Getter
@Setter
public class LuckPrizeProbabilityStrategyVO implements Serializable {
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

    private List<LuckPrizeProbabilityVO> prizeList;

    @Data
    @NoArgsConstructor
    public static class LuckPrizeProbabilityVO {
        private long prizeId;
        private int priority;
        private String rewardName;

        public LuckPrizeProbabilityVO(long prizeId, int priority) {
            this.prizeId = prizeId;
            this.priority = priority;
        }
    }

    private static final long serialVersionUID = 2413753854099023499L;
}