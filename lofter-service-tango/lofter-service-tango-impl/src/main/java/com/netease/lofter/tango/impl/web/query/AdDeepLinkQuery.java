package com.netease.lofter.tango.impl.web.query;

import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdDeepLinkQuery extends BaseQuery {

    private static final long serialVersionUID = 6590953920843990861L;
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
     * 渠道
     */
    private String channel;

}
