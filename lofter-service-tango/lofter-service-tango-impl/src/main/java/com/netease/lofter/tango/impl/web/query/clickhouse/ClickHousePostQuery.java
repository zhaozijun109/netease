package com.netease.lofter.tango.impl.web.query.clickhouse;

import com.google.common.collect.Lists;
import com.netease.lofter.tango.impl.util.NosUtils;
import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import com.netease.mm.tk.common.util.locale.date.DateUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ClickHousePostQuery extends BaseQuery {

    private static final long serialVersionUID = -7703510543470648806L;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向
     */
    private String sortOrder;

    /**
     * 文章标签
     */
    private String tags;

    /**
     * 标签关系
     */
    @NotNull(message = "tagOr missing")
    private Boolean tagOr = false;

    private List<String> timeRange;

    /**
     * 批量上传文章id列表nos文件
     */
    private String postIdUrl;

    /**
     * 批量上传文章链接nos文件
     */
    private String postLinkUrl;

    /**
     * 批量上传博客id列表nos文件
     */
    private String blogIdUrl;

    /**
     * 批量上传博客昵称
     */
    private String blogNickNameUrl;

    /**
     * 批量上传博客名称或链接
     */
    private String blogNameUrl;

    /**
     * 创作者等级
     */
    private String creatorLevel;

    /**
     * 作品类型
     */
    private String contentType;

    /**
     * 创作类型
     */
    private String postMainContentType;

    /**
     * 是否达人
     */
    private String daren;

    /**
     * 审核状态
     */
    private String recStatus;

    /**
     * 粉丝数下限
     */
    private Long fansRangeLower;

    /**
     * 粉丝数上线
     */
    private Long fansRangeUpper;

    /**
     * 是否优质内容
     */
    private String premium;

    /**
     * 回礼类型
     */
    private String returnGiftPlanType;

    /**
     * 付费状态
     */
    private String payStatus;

    public Map<String, Object> buildMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("yesterday", DateUtils.format(DateUtils.zonedDateTime().minusDays(1), DateUtils.YMD));
        params.put("tags", splitTags(tags));
        params.put("tagOr", tagOr);
        params.put("startDate", getDate(0));
        params.put("endDate", getDate(1));
        params.put("offset", getOffset());
        params.put("limit", getLimit());
        params.put("sortField", getSortField());
        params.put("sortOrder", getSortOrder());

        params.put("postIdUrl", NosUtils.transferInner(getPostIdUrl()));
        params.put("postLinkUrl", NosUtils.transferInner(getPostLinkUrl()));
        params.put("blogIdUrl", NosUtils.transferInner(getBlogIdUrl()));
        params.put("blogNameUrl", NosUtils.transferInner(getBlogNameUrl()));
        params.put("blogNickNameUrl", NosUtils.transferInner(getBlogNickNameUrl()));

        params.put("creatorLevel", getCreatorLevel());
        params.put("contentType", getContentType());
        params.put("postMainContentType", getPostMainContentType());
        params.put("daren", getDaren());
        params.put("recStatus", getRecStatus());
        params.put("fansRangeLower", getFansRangeLower());
        params.put("fansRangeUpper", getFansRangeUpper());
        params.put("premium", getPremium());
        params.put("returnGiftPlanType", getReturnGiftPlanType());
        params.put("payStatus", getPayStatus());

        return params;
    }

    private String getDate(int index) {
        if (CollectionUtils.isEmpty(timeRange)) {
            return null;
        }
        return timeRange.get(index);
    }


    private List<String> splitTags(String str) {
        if (str == null) {
            return null;
        }
        str = str.replaceAll("，", ",");
        return Lists.newArrayList(StringUtils.commaDelimitedListToSet(str));
    }


}
