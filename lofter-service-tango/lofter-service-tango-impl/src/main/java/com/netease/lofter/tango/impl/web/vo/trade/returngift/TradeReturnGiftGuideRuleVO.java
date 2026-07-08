package com.netease.lofter.tango.impl.web.vo.trade.returngift;

import java.io.Serializable;

import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import lombok.Getter;
import lombok.Setter;


/**
* 回礼引导配置
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class TradeReturnGiftGuideRuleVO implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键
    */
    private Long id;
    /**
    * 规则类型
    */
    private Long ruleType;
    /**
    * 文章类型
    */
    private Long postType;
    /**
    * 引导语
    */
    private String tip;
    /**
     * 规则描述
     */
    private String ruleDesc;
    /**
    * 提示配图
    */
    private String tipImg;
    /**
    * 图片信息
    */
    private String rule;
    /**
    * 状态（-1：删除；0：未生效；1：生效；）
    */
    private Integer status;
    /**
    * 创建时间
    */
    private Long createTime;
    private Integer priority;

    private String judgmentResult;
}