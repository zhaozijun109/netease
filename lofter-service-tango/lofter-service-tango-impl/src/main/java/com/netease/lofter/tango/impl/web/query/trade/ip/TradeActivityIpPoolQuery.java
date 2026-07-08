package com.netease.lofter.tango.impl.web.query.trade.ip;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 活动ip池
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeActivityIpPoolQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    private Long id;
    /**
    * 活动id
    */
    private Long actId;
    /**
    * ip池
    */
    private String ips;
    /**
    * 活动开始时间
    */
    private Long startTime;
    /**
    * 活动结束时间
    */
    private Long endTime;
    private Long createTimeBegin;
    private Long createTimeEnd;

    /**
     * 业务id
     */
    private Long businessId;

    /**
     * 绑定类型
     */
    private Integer bindType;
}