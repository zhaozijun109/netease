package com.netease.lofter.tango.impl.service;

import com.netease.lofter.tango.impl.delegate.TangoAppDelegate;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.TangoAppDO;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.web.query.TangoAppQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.config.TangoAppAddVO;
import com.netease.lofter.tango.impl.web.vo.config.TangoAppVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TangoAppService {

    @Autowired
    private TangoAppDelegate tangoAppDelegate;

    public List<TangoAppVO> listAll() {
        return this.populate2VOList(tangoAppDelegate.listAll());
    }

    public PageResult<TangoAppVO> list(TangoAppQuery query) {
        PageResult<TangoAppVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TangoAppDO> pageDO = tangoAppDelegate.listByQuery(query.getAppId(),
                query.getOffset(), query.getLimit());
        return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }

    private List<TangoAppVO> populate2VOList(List<TangoAppDO> list) {
        return BeanConvertUtils.convertList(list, TangoAppVO.class);
    }

    public void add(TangoAppAddVO tangoAppAddVO) {
        TangoAppDO tangoAppDO = tangoAppDelegate.selectByAppId(tangoAppAddVO.getAppId());
        AssertUtils.isTrue(tangoAppDO == null, "应用已存在");
        TangoAppDO appDO = BeanConvertUtils.convertBean(tangoAppAddVO, TangoAppDO.class);
        appDO.setCreateTime(System.currentTimeMillis());
        appDO.setUpdateTime(appDO.getCreateTime());
        tangoAppDelegate.insertValue(appDO);
    }
}
