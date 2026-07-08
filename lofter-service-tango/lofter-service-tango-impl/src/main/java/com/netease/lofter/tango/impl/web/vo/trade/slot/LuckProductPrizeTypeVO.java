package com.netease.lofter.tango.impl.web.vo.trade.slot;

import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>Title:  </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>@author: jetbi</p>
 * <p>@Create Time: 2024-07-23 10:18:27</p>
 * 此实体PO对应表:  Luck_ProductPrizeType
 */
@Getter
@Setter
public class LuckProductPrizeTypeVO implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 产品标识
     */
    private String appKey;

    /**
     * 自定义type值
     */
    private Integer type;

    /**
     * 活动名称
     */
    private String name;

    /**
     * 状态：-1，无效；0，正常；
     */
    private Byte status;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 自定义key信息
     */
    private String customInfo;

    private static final long serialVersionUID = 1130320899442359484L;
}