package com.netease.lofter.tango.impl.entity.trade.activity;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import com.netease.yaolu.commons.spring.mybatis.annotation.Column;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import com.netease.yaolu.commons.spring.mybatis.annotation.Key;

/**
* 内容付费-奖品
* generate by yaolu mybatis generator
*/
@Getter
@Setter
@FieldNameConstants
@Table("PaidContent_ActivityReward")
public class PaidContentActivityReward implements Serializable {
    private static final long serialVersionUID = -1;
    public static final int STATUS_NORMAL = 0;
    public static final int STATUS_OFFLINE = -1;

    public static final int TYPE_FREEGIFT = 1;
    public static final int TYPE_EXCHANGE_COUPON = 0;
    public static final int TYPE_DRESSING_SUIT = 2;
    public static final int TYPE_DRESSING_PART_COMMENT = 3;
    public static final int TYPE_BENEFIT_PRODUCT = 4;

    public static final int TYPE_SLOT_COUPON = 5;
    public static final int TYPE_SLOT_PROP_COUPON = 6;
    public static final int TYPE_ZHAN_ZHAN_CARD = 7;
    public static final int TYPE_EXCHANGE_COUPON_PACK = 8;
    @Deprecated
    public static final int TYPE_EXCHANGE_COUPON_CARD = 9;
    public static final int TYPE_IMAGE_LOOT_BOX_COUPON = 10;
    public static final int TYPE_UNDO_CARD = 11;
    //仅占位图片，如本次活动 微信红包封面奖品，不进行发放逻辑操作
    public static final int TYPE_IMG_PLACEHOLDER = 12;
    public static final int TYPE_AVATAR_BOX = 13;

    public static final int TYPE_IP_COUPON = 14;
    /**
     * 投票机会
     */
    public static final int TYPE_VOTE_CHANCE = 15;



    /**
    * 主键id
    */
    @Key
    @Column(name="id", value = "seq")
    private Long id;
    /**
    * 名称
    */
    @Column(name="name")
    private String name;
    /**
    * 活动id
    */
    @Column(name="actId")
    private Long actId;
    /**
    * 活动任务id
    */
    @Column(name="taskId")
    private Long taskId;
    /**
    * 配图
    */
    @Column(name="img")
    private String img;
    /**
    * 奖品类型
    */
    @Column(name="type")
    private Integer type;
    /**
    * 状态 0:正常; -1:停发（处理一些突发情况）
    */
    @Column(name="status")
    private Integer status;
    /**
    * 奖品发放数量
    */
    @Column(name="count")
    private Integer count;
    /**
    * 活动id
    */
    @Column(name="targetId")
    private Long targetId;
    /**
    * 奖励跳转链接
    */
    @Column(name="rewardSchema")
    private String rewardSchema;
    /**
    * 奖励描述文案
    */
    @Column(name="tip")
    private String tip;
    /**
    * 奖励私信文案模板
    */
    @Column(name="message")
    private String message;
    @Column(name="rewardRank")
    private Integer rewardRank;
    @Column(name="ext")
    private String ext;

}