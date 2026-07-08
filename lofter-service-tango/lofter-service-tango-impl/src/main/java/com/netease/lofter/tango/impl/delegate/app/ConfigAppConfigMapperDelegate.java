package com.netease.lofter.tango.impl.delegate.app;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.app.ConfigAppConfig.Fields;

import com.netease.lofter.tango.impl.entity.app.ConfigAppConfig;
import com.netease.lofter.tango.impl.mapper.app.ConfigAppConfigMapper;

@Primary
@Service
public class ConfigAppConfigMapperDelegate implements CommonService<ConfigAppConfigMapper, ConfigAppConfig>, DeleteCommonService<ConfigAppConfigMapper, ConfigAppConfig>{

    public PageDO<ConfigAppConfig> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                String description,
                                                Integer platform,
                                                String startVersion,
                                                String endVersion,
                                                String configKey,
                                                String configValue,
                                                Integer status,
                                                Long createTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, description, platform, startVersion, endVersion, configKey, configValue, status, createTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<ConfigAppConfig> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String description,
                            Integer platform,
                            String startVersion,
                            String endVersion,
                            String configKey,
                            String configValue,
                            Integer status,
                            Long createTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(description)) {
            sqlSelect.andEquals(Fields.description, description);
        }
        if (platform != null) {
            sqlSelect.andEquals(Fields.platform, platform);
        }
        if (StringUtils.isNotBlank(startVersion)) {
            sqlSelect.andEquals(Fields.startVersion, startVersion);
        }
        if (StringUtils.isNotBlank(endVersion)) {
            sqlSelect.andEquals(Fields.endVersion, endVersion);
        }
        if (StringUtils.isNotBlank(configKey)) {
            sqlSelect.andEquals(Fields.configKey, configKey);
        }
        if (StringUtils.isNotBlank(configValue)) {
            sqlSelect.andEquals(Fields.configValue, configValue);
        }
        if (status != null) {
            sqlSelect.andEquals(Fields.status, status);
        }
        if (createTime != null) {
            sqlSelect.andEquals(Fields.createTime, createTime);
        }
    }

    private void initTimeQuery(SqlSelect sqlSelect, Long createTimeBegin, Long createTimeEnd) {
        if (createTimeBegin != null && createTimeEnd != null) {
            sqlSelect.andGreatThanEqual(Fields.createTime, createTimeBegin);
            sqlSelect.andLessThanEqual(Fields.createTime, createTimeEnd);
        } else if (createTimeEnd != null) {
            sqlSelect.andLessThanEqual(Fields.createTime, createTimeEnd);
        } else if (createTimeBegin != null) {
            sqlSelect.andGreatThanEqual(Fields.createTime, createTimeBegin);
        }
    }
}