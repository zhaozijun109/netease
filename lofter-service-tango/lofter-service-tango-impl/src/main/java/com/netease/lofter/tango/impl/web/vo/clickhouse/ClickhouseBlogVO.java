package com.netease.lofter.tango.impl.web.vo.clickhouse;
import lombok.Getter;
import lombok.Setter;
import org.joda.time.DateTime;

import java.io.Serializable;

@Getter
@Setter
public class ClickhouseBlogVO implements Serializable {
    private static final long serialVersionUID = -5591007827019278471L;

    /**
     * 日期
     */
    private String dt;

    /**
     * blogId
     */
    private Long blogId;

    private String blog_url,daren_names,blog_name,level,post_main_content_type,blog_nickname,creator_center_level,first_publish_date,last_publish_date,registerDate;

    /**
     * 其他指标
     */
    private Long fans_std,post_count_std,is_premium,create_time,premium_post_count,text_post_count_std,photo_post_count_std,video_post_count_std,pay_post_count,receive_hot_std,receive_recommend_cnt,fans_cnt_7d,fans_cnt_30d;
}
