package com.netease.lofter.tango.impl.web.vo.trade.ip;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 活动ip池
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeActivityIpPoolVO implements Serializable {
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
    /**
     * 业务id
     */
    private Long businessId;


    /**
     * 绑定类型
     */
    private Integer bindType;


}