package com.netease.lofter.tango.impl.web.query.trade.highnet;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
* 高净值用户奖励任务
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeHighNetWorthLevelTaskQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    private Long id;
    /**
    * 人群包Id
    */
    private Long crownId;
    /**
    * 类型，0：基础权益；1: 进阶
    */
    private Integer type;
    /**
    * 等级
    */
    private Integer level;
    /**
    * 目标赞助值
    */
    private BigDecimal targetValue;
    /**
    * 奖励信息
    */
    private String rightInfoJson;
    /**
    * 状态；0：正常；-1：删除
    */
    private Integer status;
    /**
    * 加入时间
    */
    private Long createTime;
    private Long createTimeBegin;
    private Long createTimeEnd;
}