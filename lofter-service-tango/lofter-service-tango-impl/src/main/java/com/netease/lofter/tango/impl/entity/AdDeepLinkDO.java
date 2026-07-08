package com.netease.lofter.tango.impl.entity;

import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;
import java.sql.Timestamp;

@FieldNameConstants
@Table("Lofter_Deeplink")
@Getter
@Setter
public class AdDeepLinkDO implements Serializable {

    private static final long serialVersionUID = -8664344993350912789L;

    /**
     * 主键
     */
    @Key
    private Long id;

    /**
     * 创建时间
     */
    private Long createTime;

    /**
     * 更新时间
     */
    private Long updateTime;

    /**
     * 数据库更新时间
     */
    private Timestamp dbUpdateTime;

    /**
     * 广告主ID（广告账户ID）
     */
    private String advertiseId;

    /**
     * 广告计划ID
     */
    private String campaignId;

    /**
     * 广告（组）ID
     */
    private String aid;

    /**
     * 广告创意ID
     */
    private String cid;

    /**
     * 广告素材ID
     */
    private String mid;

    /**
     * 链接地址
     */
    private String url;

    /**
     * 渠道
     */
    private String channel;

    /**
     * 操作人
     */
    private String operator;

}
