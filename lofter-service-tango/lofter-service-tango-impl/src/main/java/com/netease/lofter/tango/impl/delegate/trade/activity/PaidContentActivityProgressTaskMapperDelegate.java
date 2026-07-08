package com.netease.lofter.tango.impl.delegate.trade.activity;

import com.netease.lofter.tango.impl.entity.PageDO;

import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import java.util.List;

import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityProgressTask.Fields;

import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityProgressTask;
import com.netease.lofter.tango.impl.mapper.trade.activity.PaidContentActivityProgressTaskMapper;

@Primary
@Service
public class PaidContentActivityProgressTaskMapperDelegate implements CommonService<PaidContentActivityProgressTaskMapper, PaidContentActivityProgressTask>, DeleteCommonService<PaidContentActivityProgressTaskMapper, PaidContentActivityProgressTask>{

    public PageDO<PaidContentActivityProgressTask> listByQuery(
                                                Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                String name,
                                                Long actId,
                                                String img,
                                                Integer goal,
                                                Integer status,
                                                Integer eventType,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect().limit(limit).offset(offset);
        this.buildQuery(sqlSelect, id, name, actId, img, goal, status, eventType,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        List<PaidContentActivityProgressTask> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String name,
                            Long actId,
                            String img,
                            Integer goal,
                            Integer status,
                            Integer eventType,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(name)) {
            sqlSelect.andEquals(Fields.name, name);
        }
        if (actId != null) {
            sqlSelect.andEquals(Fields.actId, actId);
        }
        if (StringUtils.isNotBlank(img)) {
            sqlSelect.andEquals(Fields.img, img);
        }
        if (goal != null) {
            sqlSelect.andEquals(Fields.goal, goal);
        }
        if (status != null) {
            sqlSelect.andEquals(Fields.status, status);
        }
        if (eventType != null) {
            sqlSelect.andEquals(Fields.eventType, eventType);
        }
        sqlSelect.orderAsc("dbCreateTime");
    }

    private void initTimeQuery(SqlSelect sqlSelect, Long createTimeBegin, Long createTimeEnd) {
    }
}