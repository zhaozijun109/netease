package com.netease.lofter.tango.impl.service;

import com.netease.lofter.tango.impl.delegate.AdDeepLinkDelegate;
import com.netease.lofter.tango.impl.entity.AdDeepLinkDO;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;
import com.netease.lofter.tango.impl.web.query.AdDeepLinkQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.SelectVO;
import com.netease.lofter.tango.impl.web.vo.ad.AdDeepLinkVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdDeepLinkService {

    @Autowired
    private AdDeepLinkDelegate adDeepLinkDelegate;


    public PageResult<AdDeepLinkVO> listByQuery(AdDeepLinkQuery adDeepLinkQuery) {
        PageResult<AdDeepLinkVO> pageResult = new PageResult<>(adDeepLinkQuery.getPage());
        PageDO<AdDeepLinkDO> pageDO = adDeepLinkDelegate.listByQuery(adDeepLinkQuery.getChannel(), adDeepLinkQuery.getAdvertiseId(),
                adDeepLinkQuery.getCampaignId(), adDeepLinkQuery.getAid(), adDeepLinkQuery.getCid(), adDeepLinkQuery.getMid(),
                adDeepLinkQuery.getOffset(), adDeepLinkQuery.getLimit());
        return pageResult.total(pageDO.getTotal())
                .list(BeanConvertUtils.convertList(pageDO.getList(), AdDeepLinkVO.class));
    }


    public boolean add(AdDeepLinkVO adDeepLinkVO) {
        adDeepLinkVO.validate();
        AdDeepLinkDO deepLinkDO = BeanConvertUtils.convertBean(adDeepLinkVO, AdDeepLinkDO.class);
        deepLinkDO.setCreateTime(System.currentTimeMillis());
        deepLinkDO.setUpdateTime(deepLinkDO.getCreateTime());
        if (null == deepLinkDO.getAdvertiseId()) {
            deepLinkDO.setAdvertiseId("");
        }
        if (null == deepLinkDO.getCampaignId()) {
            deepLinkDO.setCampaignId("");
        }
        adDeepLinkDelegate.insertValue(deepLinkDO);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        adDeepLinkDelegate.deleteById(id);
        return true;
    }

    public boolean update(AdDeepLinkVO adDeepLinkVO) {
        AssertUtils.notNull(adDeepLinkVO.getId(), "id missing");
        AdDeepLinkDO deepLinkDO = adDeepLinkDelegate.selectById(adDeepLinkVO.getId());
        AssertUtils.notNull(deepLinkDO, "id illegal");
        BeanUtils.copyNonNullProperties(adDeepLinkVO, deepLinkDO);
        deepLinkDO.setUpdateTime(System.currentTimeMillis());
        adDeepLinkDelegate.updateValue(deepLinkDO);
        return true;
    }

    public List<SelectVO> listAllChannel() {
        List<SelectVO> result = new ArrayList<>();
        result.add(new SelectVO("抖音", "toutiao2"));
        result.add(new SelectVO("快手", "kuaishou"));
        result.add(new SelectVO("B站", "bilibili"));
        result.add(new SelectVO("云音乐", "music"));
        result.add(new SelectVO("快手-聚星", "ks_jvxing"));
        result.add(new SelectVO("小红书", "xhs"));
        return result;
    }


}
