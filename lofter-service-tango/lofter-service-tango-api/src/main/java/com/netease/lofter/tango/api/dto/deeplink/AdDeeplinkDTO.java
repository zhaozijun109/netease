package com.netease.lofter.tango.api.dto.deeplink;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class AdDeeplinkDTO implements Serializable {
    private static final long serialVersionUID = -7580910050973682621L;

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

}
