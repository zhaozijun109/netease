package com.netease.lofter.tango.impl.web.vo.ad;

import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.web.vo.BaseVO;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AdDeepLinkVO extends BaseVO {

    private static final long serialVersionUID = -8664344993350912789L;

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
    @NotBlank(message = "url missing")
    private String url;

    /**
     * 渠道
     */
    @NotBlank(message = "channel missing")
    private String channel;

    /**
     * 操作人
     */
    private String operator;

    public void validate() {
        boolean allBlank = StringUtils.isBlank(advertiseId)
                && StringUtils.isBlank(campaignId)
                && StringUtils.isBlank(aid)
                && StringUtils.isBlank(cid)
                && StringUtils.isBlank(mid)
                && StringUtils.isBlank(channel);
        AssertUtils.isTrue(!allBlank, "广告主、广告计划、广告组、创意ID或素材ID不能同时为空");
    }
}
