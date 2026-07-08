package com.netease.lofter.tango.impl.delegate.statis;


import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.statis.AdChannelConfig;
import com.netease.lofter.tango.impl.entity.statis.AdChannelConfig.Fields;
import com.netease.lofter.tango.impl.mapper.statis.AdChannelConfigMapper;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-6-12 18:44:45</p>
 */
@Service
public class AdChannelConfigDelegate implements CommonDeleteDelegate<AdChannelConfigMapper, AdChannelConfig> {

    public PageDO<AdChannelConfig> listByQuery(Long createTimeBegin, Long createTimeEnd,
                                               Long id, String appId, String media, String advertiserId, String proxy, String channelPackage, int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, appId, media, advertiserId, proxy, channelPackage, createTimeBegin, createTimeEnd);
        int count = count(sqlSelect);
        if (count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.id).offset(offset).limit(limit);
        List<AdChannelConfig> list = selectList(sqlSelect);
        return new PageDO<>(count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String appId,
                            String media,
                            String advertiserId,
                            String proxy,
                            String channelPackage,
                            Long createTimeBegin, Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(appId)) {
            sqlSelect.andEquals(Fields.appId, appId);
        }
        if (StringUtils.isNotBlank(media)) {
            sqlSelect.andEquals(Fields.media, media);
        }
        if (StringUtils.isNotBlank(advertiserId)) {
            sqlSelect.andEquals(Fields.advertiserId, advertiserId);
        }
        if (StringUtils.isNotBlank(proxy)) {
            sqlSelect.andEquals(Fields.proxy, proxy);
        }
        if (StringUtils.isNotBlank(channelPackage)) {
            sqlSelect.andEquals(Fields.channelPackage, channelPackage);
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

    public AdChannelConfig selectByAdvertiserId(String advertiserId, String media, String proxy, String channelPackage) {
        return selectOne(new SqlSelect()
                .whenEquals(Fields.advertiserId, advertiserId)
                .andEquals(Fields.channelPackage, channelPackage)
                .andEquals(Fields.media, media)
                .andEquals(Fields.proxy, proxy));
    }

}







