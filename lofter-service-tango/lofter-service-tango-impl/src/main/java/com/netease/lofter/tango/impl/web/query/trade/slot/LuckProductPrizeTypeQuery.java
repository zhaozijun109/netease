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
 * <p>@Create Time: 2024-07-23 10:18:27</p>
 * 此实体PO对应表:  Luck_ProductPrizeType
 */
@Getter
@Setter
public class LuckProductPrizeTypeQuery extends BaseQuery implements Serializable {
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
     * 自定义key信息
     */
    private String customInfo;

    private Long createTimeBegin;

    private Long createTimeEnd;

    private static final long serialVersionUID = -4782403678659338830L;
}