package com.netease.lofter.tango.impl.web.query.trade.activity;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;


/**
* 内容付费-奖品
* generate by yaolu mybatis generator
*/
@Getter
@Setter
public class PaidContentActivityRewardQuery extends BaseQuery implements Serializable {
    private static final long serialVersionUID = -1;

    /**
    * 主键id
    */
    private Long id;
    /**
    * 名称
    */
    private String name;
    /**
    * 活动id
    */
    private Long actId;
    /**
    * 活动任务id
    */
    private Long taskId;
    /**
    * 配图
    */
    private String img;
    /**
    * 奖品类型
    */
    private Integer type;
    /**
    * 状态 0:正常; -1:停发（处理一些突发情况）
    */
    private Integer status;
    /**
    * 奖品发放数量
    */
    private Integer count;
    /**
    * 活动id
    */
    private Long targetId;
    /**
    * 奖励跳转链接
    */
    private String rewardSchema;
    /**
    * 奖励描述文案
    */
    private String tip;
    /**
    * 奖励私信文案模板
    */
    private String message;
    private Long createTimeBegin;
    private Long createTimeEnd;

    /**
     * 场景
     * 查询抽奖奖品时使用
     */
    private String scene;

}