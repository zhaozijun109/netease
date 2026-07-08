
package com.netease.lofter.tango.impl.delegate.trade.slot;


import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckProductPrizeType;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckProductPrizeType.Fields;
import com.netease.lofter.tango.impl.mapper.trade.slot.LuckProductPrizeTypeMapper;
import com.netease.lofter.tango.impl.entity.PageDO;

import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.springframework.stereotype.Service;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

 /**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: shiliang</p>
 * <p>@Create Time: 2024-7-23 10:18:28</p>
 */
@Service
public class LuckProductPrizeTypeDelegate implements CommonDeleteDelegate<LuckProductPrizeTypeMapper, LuckProductPrizeType> {

    public PageDO<LuckProductPrizeType> listByQuery(Long createTimeBegin, Long createTimeEnd,
        Long id, String appKey, Integer type, String name, String customInfo,  int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, appKey, type, name, customInfo,  createTimeBegin, createTimeEnd);
        int count = count(sqlSelect);
        if (count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<LuckProductPrizeType> list = selectList(sqlSelect);
        return new PageDO<>(count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String appKey,
                            Integer type,
                            String name,
                            String customInfo,
                            Long createTimeBegin, Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(appKey)) {
            sqlSelect.andEquals(Fields.appKey, appKey);
        }
        if (type != null) {
            sqlSelect.andEquals(Fields.type, type);
        }
        if (StringUtils.isNotBlank(name)) {
            sqlSelect.andEquals(Fields.name, name);
        }
        if (StringUtils.isNotBlank(customInfo)) {
            sqlSelect.andEquals(Fields.customInfo, customInfo);
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







