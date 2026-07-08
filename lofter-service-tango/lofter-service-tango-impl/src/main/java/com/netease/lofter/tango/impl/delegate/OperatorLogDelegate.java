package com.netease.lofter.tango.impl.delegate;


import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.OperatorLog;
import com.netease.lofter.tango.impl.entity.OperatorLog.Fields;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.mapper.OperatorLogMapper;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: guleiyang</p>
 * <p>@Create Time: 2024-6-5 20:34:55</p>
 */
@Service
public class OperatorLogDelegate implements CommonDeleteDelegate<OperatorLogMapper, OperatorLog> {

    public PageDO<OperatorLog> listByQuery(Long createTimeBegin, Long createTimeEnd,
                                           Long id, String controller, String method, String operator, String params, int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, controller, method, operator, params, createTimeBegin, createTimeEnd);
        int count = count(sqlSelect);
        if (count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<OperatorLog> list = selectList(sqlSelect);
        return new PageDO<>(count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String controller,
                            String method,
                            String operator,
                            String params,
                            Long createTimeBegin, Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(controller)) {
            sqlSelect.andEquals(Fields.controller, controller);
        }
        if (StringUtils.isNotBlank(method)) {
            sqlSelect.andEquals(Fields.method, method);
        }
        if (StringUtils.isNotBlank(operator)) {
            sqlSelect.andLike(Fields.operator, operator + "%");
        }
        if (StringUtils.isNotBlank(params)) {
            sqlSelect.andEquals(Fields.params, params);
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







