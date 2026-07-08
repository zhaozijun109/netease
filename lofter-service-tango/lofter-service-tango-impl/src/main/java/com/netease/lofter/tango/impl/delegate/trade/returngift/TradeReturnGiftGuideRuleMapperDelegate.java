package com.netease.lofter.tango.impl.delegate.trade.returngift;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.returngift.TradeReturnGiftGuideRule.Fields;

import com.netease.lofter.tango.impl.entity.trade.returngift.TradeReturnGiftGuideRule;
import com.netease.lofter.tango.impl.mapper.trade.returngift.TradeReturnGiftGuideRuleMapper;

@Primary
@Service
public class TradeReturnGiftGuideRuleMapperDelegate implements CommonService<TradeReturnGiftGuideRuleMapper, TradeReturnGiftGuideRule>, DeleteCommonService<TradeReturnGiftGuideRuleMapper, TradeReturnGiftGuideRule>{

    public PageDO<TradeReturnGiftGuideRule> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                Long ruleType,
                                                Long postType,
                                                String tip,
                                                String tipImg,
                                                String rule,
                                                Integer status,
                                                Long createTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, ruleType, postType, tip, tipImg, rule, status, createTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TradeReturnGiftGuideRule> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            Long ruleType,
                            Long postType,
                            String tip,
                            String tipImg,
                            String rule,
                            Integer status,
                            Long createTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (ruleType != null) {
            sqlSelect.andEquals(Fields.ruleType, ruleType);
        }
        if (postType != null) {
            sqlSelect.andEquals(Fields.postType, postType);
        }
        if (StringUtils.isNotBlank(tip)) {
            sqlSelect.andEquals(Fields.tip, tip);
        }
        if (StringUtils.isNotBlank(tipImg)) {
            sqlSelect.andEquals(Fields.tipImg, tipImg);
        }
        if (StringUtils.isNotBlank(rule)) {
            sqlSelect.andEquals(Fields.rule, rule);
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