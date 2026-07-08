package com.netease.lofter.tango.impl.delegate.trade.highnet;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.trade.highnet.TradeHighNetWorthLevelTask;
import com.netease.lofter.tango.impl.entity.trade.highnet.TradeHighNetWorthLevelTask.Fields;
import com.netease.lofter.tango.impl.mapper.trade.highnet.TradeHighNetWorthLevelTaskMapper;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlUpdate;
import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Primary
@Service
public class TradeHighNetWorthLevelTaskMapperDelegate implements CommonService<TradeHighNetWorthLevelTaskMapper, TradeHighNetWorthLevelTask>, DeleteCommonService<TradeHighNetWorthLevelTaskMapper, TradeHighNetWorthLevelTask>{

    public PageDO<TradeHighNetWorthLevelTask> listByQuery(Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                Long crownId,
                                                Integer type,
                                                Integer level,
                                                BigDecimal targetValue,
                                                String rightInfoJson,
                                                Integer status,
                                                Long createTime,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, crownId, type, level, targetValue, rightInfoJson, status, createTime,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<TradeHighNetWorthLevelTask> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            Long crownId,
                            Integer type,
                            Integer level,
                            BigDecimal targetValue,
                            String rightInfoJson,
                            Integer status,
                            Long createTime,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (crownId != null) {
            sqlSelect.andEquals(Fields.crownId, crownId);
        }
        if (type != null) {
            sqlSelect.andEquals(Fields.type, type);
        }
        if (level != null) {
            sqlSelect.andEquals(Fields.level, level);
        }
        if (targetValue != null) {
            sqlSelect.andEquals(Fields.targetValue, targetValue);
        }
        if (StringUtils.isNotBlank(rightInfoJson)) {
            sqlSelect.andEquals(Fields.rightInfoJson, rightInfoJson);
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


     public int updateStatus(long id, int status) {
         if (id <= 0) {
             return 0;
         }
         SqlUpdate sqlUpdate = new SqlUpdate().setLiteral("status", status).whenEquals("id", id);
         return update(sqlUpdate, null);
     }
}