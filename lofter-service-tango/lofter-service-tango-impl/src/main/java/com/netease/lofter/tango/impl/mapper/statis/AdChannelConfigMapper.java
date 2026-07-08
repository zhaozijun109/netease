package com.netease.lofter.tango.impl.mapper.statis;

import com.netease.lofter.tango.impl.config.DbConfig;
import com.netease.lofter.tango.impl.entity.statis.AdChannelConfig;
import com.netease.yaolu.commons.datasource.dynamic.DataSource;
import com.netease.yaolu.commons.spring.mybatis.mapper.CommonMapper;
import com.netease.yaolu.commons.spring.mybatis.mapper.DeleteCommonMapper;
import org.apache.ibatis.annotations.Mapper;

@DataSource(DbConfig.COMIC_STATIS_DATASOURCE)
@Mapper
public interface AdChannelConfigMapper extends DeleteCommonMapper<AdChannelConfig>, CommonMapper<AdChannelConfig> {
}