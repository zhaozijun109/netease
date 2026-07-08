package com.netease.lofter.tango.impl.web.query.clickhouse;

import com.google.common.collect.Lists;
import com.netease.lofter.tango.impl.web.ro.BaseQuery;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ClickHouseIpQuery extends BaseQuery {
    private static final long serialVersionUID = -5543284904328650121L;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序方向
     */
    private String sortOrder;

    /**
     * ip
     */
    private String ips;

    /**
     * 日期范围
     */
    private List<String> timeRange;

    public Map<String, Object> buildMap() {
        Map<String, Object> params = new HashMap<>();
        params.put("ips", splitIps(ips));
        params.put("startDate", getDate(0));
        params.put("endDate", getDate(1));
        params.put("offset", getOffset());
        params.put("limit", getLimit());
        params.put("sortField", getSortField());
        params.put("sortOrder", getSortOrder());
        return params;
    }

    private String getDate(int index) {
        if (CollectionUtils.isEmpty(timeRange)) {
            return null;
        }
        return timeRange.get(index);
    }

    private List<String> splitIps(String str) {
        if (str == null) {
            return null;
        }
        str = str.replaceAll("，", ",");
        return Lists.newArrayList(StringUtils.commaDelimitedListToSet(str));
    }

}
