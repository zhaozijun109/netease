package com.netease.lofter.tango.impl.entity.trade.highnet;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import java.math.BigDecimal;

/**
* 高净值用户奖励任务
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_HighNetWorthLevelTask")
public class TradeHighNetWorthLevelTask implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    @Key
    @Column(name="id",value = "seq")
    private Long id;
    /**
    * 人群包Id
    */
    @Column(name="crownId")
    private long crownId;
    /**
    * 类型，0：基础权益；1: 进阶
    */
    @Column(name="type")
    private int type;
    /**
    * 等级
    */
    @Column(name="level")
    private int level;
    /**
    * 目标赞助值
    */
    @Column(name="targetValue")
    private BigDecimal targetValue;

    @Column(name = "rightDate")
    private String rightDate;

    @Column(name = "rightTotalPrice")
    private BigDecimal rightTotalPrice;
    /**
    * 奖励信息
    */
    @Column(name="rightInfoJson")
    private String rightInfoJson;
    /**
    * 状态；0：正常；-1：删除
    */
    @Column(name="status")
    private int status;
    /**
    * 加入时间
    */
    @Column(name="createTime")
    private long createTime;
}