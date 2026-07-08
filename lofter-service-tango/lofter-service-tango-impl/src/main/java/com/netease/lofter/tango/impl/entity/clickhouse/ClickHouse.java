package com.netease.lofter.tango.impl.entity.clickhouse;

import com.netease.yaolu.commons.spring.mybatis.annotation.Key;
import com.netease.yaolu.commons.spring.mybatis.annotation.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

@Getter
@Setter
@FieldNameConstants
@Table("`tag_fetch_tag_action_dd`")
public class ClickHouse {

    /**
     * 主键
     */
    @Key
    private Long id;

    /**
     * 创建时间
     */
    private Long createTime;

}
