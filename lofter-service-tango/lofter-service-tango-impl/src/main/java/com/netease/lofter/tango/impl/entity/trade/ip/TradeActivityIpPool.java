package com.netease.lofter.tango.impl.entity.trade.ip;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;

/**
* 活动ip池
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_ActivityIpPool")
public class TradeActivityIpPool implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 活动id
    */
    @Column(name="actId")
    private Long actId;
    /**
    * ip池
    */
    @Column(name="ips")
    private String ips;
    /**
    * 活动开始时间
    */
    @Column(name="startTime")
    private Long startTime;
    /**
    * 活动结束时间
    */
    @Column(name="endTime")
    private Long endTime;

    /**
     * 业务id
     */
    @Column(name="businessId")
    private Long businessId;

    /**
     * 绑定类型,0-活动，1-券包
     */
    @Column(name="bindType")
    private Integer bindType;
}