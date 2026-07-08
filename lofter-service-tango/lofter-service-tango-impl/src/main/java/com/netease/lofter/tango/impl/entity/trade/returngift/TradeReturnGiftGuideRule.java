package com.netease.lofter.tango.impl.entity.trade.returngift;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;

/**
* 回礼引导配置
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("Trade_ReturnGiftGuideRule")
public class TradeReturnGiftGuideRule implements Serializable {
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
    * 规则类型
    */
    @Column(name="ruleType")
    private Long ruleType;
    /**
     * 规则描述
     */
    @Column(name="ruleDesc")
    private String ruleDesc;
    /**
    * 文章类型
    */
    @Column(name="postType")
    private Integer postType;
    /**
    * 引导语
    */
    @Column(name="tip")
    private String tip;
    /**
    * 提示配图
    */
    @Column(name="tipImg")
    private String tipImg;
    /**
    * 图片信息
    */
    @Column(name="rule")
    private String rule;
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
    /**
     * 优先级
     */
    @Column(name = "priority")
    private Integer priority;

    /**
     * 判定结果
     */
    @Column(name="judgmentResult")
    private String judgmentResult;
}