package com.netease.lofter.tango.impl.web.vo.clickhouse;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ClickhousePostVO implements Serializable {
    private static final long serialVersionUID = 2786430269044190139L;
    /**
     * 文章ID
     */
    private Long postId;
    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 发布日期
     */
    private String publishDate;
    /**
     * 文章链接
     */
    private String postUrl;
    /**
     * 达人认证名称
     */
    private String authenticateNames;
    /**
     * 博客昵称
     */
    private String blogNickName;
    /**
     * blogName
     */
    private String blogName;
    /**
     * 博客发文数
     */
    private Long postNum;
    /**
     * 博客粉丝数
     */
    private Long beenFollowedUV;

    /**
     * 文章所添加标签
     */
    private String tags;

    /**
     * 文章类型
     */
    private String contentType;

    /**
     * 标题
     */
    private String title;

    /**
     * 创作者等级
     */
    private String level;

    /**
     * 是否标签过滤
     */
    private String tagForbid;

    /**
     * 是否生态优质
     */
    private String premium;

    /**
     * 是否设置回礼
     */
    private Integer returnGiftSet;

    /**
     * 刷热值
     */
    private Long shuaHot;

    /**
     * 是否刷热
     */
    private Boolean ifShuaHot;

    /**
     * 总热度
     */
    private Long hot;
    /**
     * 总评论量
     */
    private Long commendCount;
    /**
     * 总喜欢量
     */
    private Long praiseCount;
    /**
     * 总转载量
     */
    private Long reproduceCount;
    /**
     * 总推荐量
     */
    private Long recommendCount;
    /**
     * 免费送礼人数
     */
    private Long freeGiftUserCount;
    /**
     * 免费礼物个数
     */
    private Long freeGiftCount;
    /**
     * 付费礼物人数
     */
    private Long payGiftUserCount;
    /**
     * 付费礼物金额
     */
    private Long payGiftAmount;

    /**
     * 文章主要内容类型
     */
    private String postMainContentType;

    /**
     * 回礼类型
     */
    private String returnGiftPlanType;

    /**
     * 付费状态
     */
    private String payStatus;

    /**
     * 发文时间
     */
    private String publishTime;

    /**
     * 审核状态
     */
    private String recomStatus;

    /**
     * 曝光量
     */
    private Long exposedCount;

    /**
     * 点击量
     */
    private Long clickCount;

    /**
     * 推荐曝光占比
     */
    private Double recTrafficRatio;

    /**
     * 冷启曝光占比
     */
    private Double coldStartTrafficRatio;

    /**
     * 发现页曝光占比
     */
     private Double feedRecTrafficRatio;

    /**
     * 标签页曝光占比
     */
    private Double tagRecTrafficRatio;

    public Boolean getIfShuaHot() {
        return shuaHot != null && shuaHot > 0;
    }
}
