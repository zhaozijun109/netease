package com.netease.lofter.tango.impl.web.query.trade.gift;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* BC资源位
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeBcSlotQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 
    */
    private Long id;
    /**
    * 人群包id
    */
    private String crowdIds;
    /**
    * 类型 0-活动
    */
    private Integer type;
    /**
    * 优先级
    */
    private Integer priority;
    /**
    * banner信息
    */
    private String banner;
    /**
    * 跳转url
    */
    private String targetUrl;
    /**
    * 生效时间
    */
    private Long startTime;
    /**
    * 失效时间
    */
    private Long endTime;
    /**
    * 状态，0-正常，1-发布上线；-1下线
    */
    private Integer status;
    /**
    * 创建时间
    */
    private Long createTime;
    private Long createTimeBegin;
    private Long createTimeEnd;
}