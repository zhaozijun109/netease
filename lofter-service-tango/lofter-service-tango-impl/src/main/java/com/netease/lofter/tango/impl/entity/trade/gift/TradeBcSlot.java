package com.netease.lofter.tango.impl.entity.trade.gift;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;

/**
* BC资源位
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_BcSlot")
public class TradeBcSlot implements Serializable {
    private static final long serialVersionUID = -1;

    public static final int STATUS_OFFLINE = -1;
    public static final int STATUS_INIT = 0;
    public static final int STATUS_ONLINE = 1;

    /**
    * 
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 人群包id
    */
    @Column(name="crowdIds")
    private String crowdIds;
    /**
    * 类型 0-活动
    */
    @Column(name="type")
    private Integer type;
    /**
    * 优先级
    */
    @Column(name="priority")
    private Integer priority;
    /**
    * banner信息
    */
    @Column(name="banner")
    private String banner;
    /**
    * 跳转url
    */
    @Column(name="targetUrl")
    private String targetUrl;
    /**
    * 生效时间
    */
    @Column(name="startTime")
    private Long startTime;
    /**
    * 失效时间
    */
    @Column(name="endTime")
    private Long endTime;
    /**
    * 状态，0-正常，1-发布上线；-1下线
    */
    @Column(name="status")
    private Integer status;
    /**
    * 创建时间
    */
    @Column(name="createTime")
    private Long createTime;
}