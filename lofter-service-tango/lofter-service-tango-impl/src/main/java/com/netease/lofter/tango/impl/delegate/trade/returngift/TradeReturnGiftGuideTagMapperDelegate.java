package com.netease.lofter.tango.impl.delegate.trade.returngift;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.returngift.TradeReturnGiftGuideTag.Fields;

import com.netease.lofter.tango.impl.entity.trade.returngift.TradeReturnGiftGuideTag;
import com.netease.lofter.tango.impl.mapper.trade.returngift.TradeReturnGiftGuideTagMapper;

@Primary
@Service
public class TradeReturnGiftGuideTagMapperDelegate implements CommonService<TradeReturnGiftGuideTagMapper, TradeReturnGiftGuideTag>, DeleteCommonService<TradeReturnGiftGuideTagMapper, TradeReturnGiftGuideTag>{

    public PageDO<TradeReturnGiftGuideTag> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                String tag,
                                                String promotion,
                                                Integer status,
                                                Integer postType,
                                                Long createTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, tag, promotion, status, postType, createTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TradeReturnGiftGuideTag> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String tag,
                            String promotion,
                            Integer status,
                            Integer postType,
                            Long createTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(tag)) {
            sqlSelect.andEquals(Fields.tag, tag);
        }
        if (StringUtils.isNotBlank(promotion)) {
            sqlSelect.andEquals(Fields.promotion, promotion);
        }
        if (status != null) {
            sqlSelect.andEquals(Fields.status, status);
        }
        if (postType != null) {
            sqlSelect.andEquals(Fields.postType, postType);
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