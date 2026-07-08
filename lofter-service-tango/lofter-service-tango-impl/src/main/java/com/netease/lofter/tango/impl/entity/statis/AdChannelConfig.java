package com.netease.lofter.tango.impl.entity.statis;

import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.io.Serializable;

/**
 * <p>Title:  </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2024</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-06-12 18:44:44</p>
 * 此实体PO对应表:  ad_channel_config
 */
@Getter
@Setter
@FieldNameConstants
@Table("`ad_channel_config`")
public class AdChannelConfig implements Serializable {
    private static final long serialVersionUID = -4153450202501504770L;
    /**
     * ID
     */
    @Key
    private Long id;
    /**
     * 产品id
     */
    private String appId;
    /**
     * 投放媒体名称
     */
    private String media;
    /**
     * 广告主ID
     */
    private String advertiserId;
    /**
     * 代理名称
     */
    private String proxy;
    /**
     * 渠道包名称
     */
    private String channelPackage;
    /**
     * 创建时间
     */
    private Long createTime;
    /**
     * 更新时间
     */
    private Long updateTime;
}