package com.netease.lofter.tango.impl.web.vo.clickhouse;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ClickhouseIpVO implements Serializable {
    private static final long serialVersionUID = -3359262970899106455L;

    /**
     * 日期
     */
    private String dt;

    /**
     * ip
     */
    private String ip;

    /**
     * 标签数
     */
    private Long tag_count;

    /**
     * 其他指标
     */
    private Long post_count,post_uv,free_post_count,photo_post_count,text_post_count,video_post_count,photo_post_uv,text_post_uv,video_post_uv,level_s_post_count,level_a_post_count,level_b_post_count,level_c_post_count,level_d_post_count,level_d_star_post_count,level_none_post_count,level_s_post_uv,level_a_post_uv,level_b_post_uv,level_c_post_uv,level_d_post_uv,level_d_star_post_uv,level_none_post_uv,hot,recommend_count,photo_hot,text_hot,video_hot,photo_recommend_count,text_recommend_count,video_recommend_count,expose_pv,real_browse_pv,premium_post_count,photo_premium_post_count,text_premium_post_count,video_premium_post_count,pay_gift_uv,pay_post_count;

    /**
     * 付费礼物金额
     */
    private Double pay_gift_money;
}
