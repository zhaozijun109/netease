package com.netease.lofter.tango.impl.web.query.clickhouse;

import com.netease.lofter.tango.impl.util.NosUtils;
import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ClickHouseBlogQuery extends BaseQuery {
    private static final long serialVersionUID = 3086498416269795502L;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向
     */
    private String sortOrder;

    /**
     * 批量上传博客id列表nos文件
     */
    private String blogIdUrl;

    /**
     * 批量上传博客链接列表nos文件
     */
    private String blogHomePageUrl;

    /**
     * 日期范围
     */
    private List<String> timeRange;

    /**
     * 首次发文日期范围
     */
    private List<String> firstPostTimeRange;

    /**
     * 最近一次发文日期范围
     */
    private List<String> lastPostTimeRange;

    /**
     * 创作者等级
     */
    private List<String> level;

    /**
     * 新创作者等级
     */
    private List<String> newLevel;

    /**
     * 粉丝数下限
     */
    private Long fansRangeLower;

    /**
     * 粉丝数上线
     */
    private Long fansRangeUpper;

    public Map<String, Object> buildMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("blogIdUrl", NosUtils.transferInner(getBlogIdUrl()));
        params.put("blogHomePageUrl", NosUtils.transferInner(getBlogHomePageUrl()));
        params.put("startDate", getDate(timeRange,0));
        params.put("endDate", getDate(timeRange,1));
        params.put("firstPostStartDate", getDate(firstPostTimeRange,0));
        params.put("firstPostEndDate", getDate(firstPostTimeRange,1));
        params.put("lastPostStartDate", getDate(lastPostTimeRange,0));
        params.put("lastPostEndDate", getDate(lastPostTimeRange,1));
        params.put("level", getLevel());
        params.put("newLevel", getNewLevel());
        params.put("fansRangeLower", getFansRangeLower());
        params.put("fansRangeUpper", getFansRangeUpper());
        params.put("offset", getOffset());
        params.put("limit", getLimit());
        params.put("sortField", getSortField());
        params.put("sortOrder", getSortOrder());
        return params;
    }

    private String getDate(List<String> range, int index) {
        if (CollectionUtils.isEmpty(range)) {
            return null;
        }
        return range.get(index);
    }
}
