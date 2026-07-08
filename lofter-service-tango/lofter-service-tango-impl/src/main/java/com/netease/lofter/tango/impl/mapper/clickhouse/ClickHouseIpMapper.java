package com.netease.lofter.tango.impl.mapper.clickhouse;

import com.netease.lofter.tango.impl.config.DbConfig;
import com.netease.lofter.tango.impl.entity.clickhouse.ClickHouse;
import com.netease.lofter.tango.impl.web.vo.clickhouse.ClickhouseIpVO;
import com.netease.yaolu.commons.datasource.dynamic.DataSource;
import com.netease.yaolu.commons.spring.mybatis.mapper.CommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@DataSource(DbConfig.CLICKHOUSE_DATASOURCE)
@Mapper
public interface ClickHouseIpMapper extends DeleteCommonMapper<ClickHouse>, CommonMapper<ClickHouse> {

    List<ClickhouseIpVO> listIp(Map<String, Object> params);
    Long countIp(Map<String, Object> params);
}
