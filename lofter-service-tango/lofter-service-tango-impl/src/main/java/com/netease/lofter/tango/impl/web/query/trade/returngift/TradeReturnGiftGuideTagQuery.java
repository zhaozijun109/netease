package com.netease.lofter.tango.impl.web.query.trade.returngift;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 回礼引导标签
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeReturnGiftGuideTagQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键
    */
    private Long id;
    /**
    * 标签
    */
    private String tag;
    /**
    * 宣传文案
    */
    private String promotion;
    /**
    * 状态（-1：删除；0：未生效；1：生效；）
    */
    private Integer status;
    /**
    * 创建时间
    */
    private Long createTime;
    private Long createTimeBegin;
    private Long createTimeEnd;
    private Integer postType;
}