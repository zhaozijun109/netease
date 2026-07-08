package com.netease.lofter.tango.impl.delegate;

import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.AdDeepLinkDO;
import com.netease.lofter.tango.impl.entity.AdDeepLinkDO.Fields;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.mapper.AdDeepLinkMapper;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 *
 */
@Service
public class AdDeepLinkDelegate implements CommonDeleteDelegate<AdDeepLinkMapper, AdDeepLinkDO> {


    public PageDO<AdDeepLinkDO> listByQuery(String channel, String advertiserId, String campaignId, String aid, String cid, String mid,
                                            int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        if (StringUtils.isNotBlank(channel)) {
            sqlSelect.andEquals(Fields.channel, channel);
        }
        if (StringUtils.isNotBlank(advertiserId)) {
            sqlSelect.andEquals(Fields.advertiseId, advertiserId);
        }
        if (StringUtils.isNotBlank(campaignId)) {
            sqlSelect.andEquals(Fields.campaignId, campaignId);
        }
        if (StringUtils.isNotBlank(aid)) {
            sqlSelect.andLike(Fields.aid, aid);
        }
        if (StringUtils.isNotBlank(cid)) {
            sqlSelect.andLike(Fields.cid, cid);
        }
        if (StringUtils.isNotBlank(mid)) {
            sqlSelect.andLike(Fields.mid, mid);
        }
        int count = count(sqlSelect);
        if (count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<AdDeepLinkDO> list = selectList(sqlSelect);
        return new PageDO<>(count, list);
    }

    public List<AdDeepLinkDO> listAll() {
        return selectListNoLimit(new SqlSelect());
    }
}
