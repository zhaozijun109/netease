package com.netease.lofter.tango.impl.entity.trade.returngift;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;

/**
* 回礼引导标签
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_ReturnGiftGuideTag")
public class TradeReturnGiftGuideTag implements Serializable {
    private static final long serialVersionUID = -1;

    public static final int STATUS_DELETE = -1;
    public static final int STATUS_OFFLINE = 0;
    public static final int STATUS_ONLINE = 1;

    /**
    * 主键
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 标签
    */
    @Column(name="tag")
    private String tag;
    /**
    * 宣传文案
    */
    @Column(name="promotion")
    private String promotion;
    /**
    * 状态（-1：删除；0：未生效；1：生效；）
    */
    @Column(name="status")
    private Integer status;
    /**
    * 创建时间
    */
    @Column(name="createTime")
    private Long createTime;

    @Column(name="postType")
    private Integer postType;
}