package com.netease.lofter.tango.impl.service;

import com.netease.lofter.tango.impl.delegate.OperatorLogDelegate;
import com.netease.lofter.tango.impl.entity.OperatorLog;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;
import com.netease.lofter.tango.impl.web.query.OperatorLogQuery;
import com.netease.lofter.tango.impl.web.vo.OperatorLogVO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class OperatorLogService {

    @Autowired
    private OperatorLogDelegate delegate;

    public PageResult<OperatorLogVO> listByQuery(OperatorLogQuery query) {
        PageResult<OperatorLogVO> pageResult = new PageResult<>(query.getPage());
        PageDO<OperatorLog> pageDO = delegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
                query.getId(),
                query.getController(),
                query.getMethod(),
                query.getOperator(),
                query.getParams(),
                query.getOffset(), query.getLimit());
        return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(OperatorLogVO operatorLogVO) {
        OperatorLog operatorLog = BeanConvertUtils.convertBean(operatorLogVO, OperatorLog.class);
        operatorLog.setCreateTime(System.currentTimeMillis());
        operatorLog.setUpdateTime(operatorLog.getCreateTime());
        delegate.insertValue(operatorLog);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = delegate.deleteById(id);
        return count == 1;
    }

    public boolean update(OperatorLogVO operatorLogVO) {
        AssertUtils.isTrue(operatorLogVO != null && operatorLogVO.getId() != null, "id missing");
        OperatorLog operatorLog = delegate.selectById(operatorLogVO.getId());
        AssertUtils.notNull(operatorLog, "id illegal");
        BeanUtils.copyNonNullProperties(operatorLogVO, operatorLog);
        operatorLog.setUpdateTime(System.currentTimeMillis());
        delegate.updateValue(operatorLog);
        return true;
    }

    private List<OperatorLogVO> populate2VOList(List<OperatorLog> list) {
        return BeanConvertUtils.convertList(list, OperatorLogVO.class);
    }


}
