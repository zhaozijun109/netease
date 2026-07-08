package com.netease.lofter.tango.impl.service;

import com.netease.lofter.tango.impl.delegate.TangoConfigOpHistoryDelegate;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.TangoConfigOpHistory;
import com.netease.lofter.tango.impl.helper.ProfileEnv;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;
import com.netease.lofter.tango.impl.web.query.TangoConfigOpHistoryQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.TangoConfigOpHistoryVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TangoConfigOpHistoryService {

    @Autowired
    private TangoConfigOpHistoryDelegate tangoConfigOpHistoryDelegate;
    @Autowired
    private ProfileEnv profileEnv;

    public PageResult<TangoConfigOpHistoryVO> listByQuery(TangoConfigOpHistoryQuery query) {
        PageResult<TangoConfigOpHistoryVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TangoConfigOpHistory> pageDO = tangoConfigOpHistoryDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
                query.getId(),
                query.getAppId(),
                query.getConfigKey(),
                query.getOperator(),
                query.getOpType(),
                query.getNewValue(),
                query.getOldValue(),
                profileEnv.env(),
                query.getOffset(), query.getLimit());
        return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(TangoConfigOpHistoryVO tangoConfigOpHistoryVO) {
        TangoConfigOpHistory tangoConfigOpHistory = BeanConvertUtils.convertBean(tangoConfigOpHistoryVO, TangoConfigOpHistory.class);
        tangoConfigOpHistory.setCreateTime(System.currentTimeMillis());
        tangoConfigOpHistory.setUpdateTime(tangoConfigOpHistory.getCreateTime());
        tangoConfigOpHistoryDelegate.insertValue(tangoConfigOpHistory);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tangoConfigOpHistoryDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(TangoConfigOpHistoryVO tangoConfigOpHistoryVO) {
        AssertUtils.isTrue(tangoConfigOpHistoryVO != null && tangoConfigOpHistoryVO.getId() != null, "id missing");
        TangoConfigOpHistory tangoConfigOpHistory = tangoConfigOpHistoryDelegate.selectById(tangoConfigOpHistoryVO.getId());
        AssertUtils.notNull(tangoConfigOpHistory, "id illegal");
        BeanUtils.copyNonNullProperties(tangoConfigOpHistoryVO, tangoConfigOpHistory);
        tangoConfigOpHistory.setUpdateTime(System.currentTimeMillis());
        tangoConfigOpHistoryDelegate.updateValue(tangoConfigOpHistory);
        return true;
    }

    private List<TangoConfigOpHistoryVO> populate2VOList(List<TangoConfigOpHistory> list) {
        return BeanConvertUtils.convertList(list, TangoConfigOpHistoryVO.class);
    }


}
