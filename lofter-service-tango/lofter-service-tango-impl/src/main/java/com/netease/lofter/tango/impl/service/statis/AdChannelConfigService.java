package com.netease.lofter.tango.impl.service.statis;

import com.netease.lofter.tango.impl.delegate.statis.AdChannelConfigDelegate;
import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.statis.AdChannelConfig;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;
import com.netease.lofter.tango.impl.web.query.statis.AdChannelConfigQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.statis.AdChannelConfigVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class AdChannelConfigService {

    @Autowired
    private AdChannelConfigDelegate delegate;

    public PageResult<AdChannelConfigVO> listByQuery(AdChannelConfigQuery query) {
        PageResult<AdChannelConfigVO> pageResult = new PageResult<>(query.getPage());
        PageDO<AdChannelConfig> pageDO = delegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
                query.getId(),
                query.getAppId(),
                query.getMedia(),
                query.getAdvertiserId(),
                query.getProxy(),
                query.getChannelPackage(),
                query.getOffset(), query.getLimit());
        return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(AdChannelConfigVO adChannelConfigVO) {
        AdChannelConfig po = delegate.selectByAdvertiserId(adChannelConfigVO.getAdvertiserId(), adChannelConfigVO.getMedia(), adChannelConfigVO.getProxy(), adChannelConfigVO.getChannelPackage());
        AssertUtils.isTrue(po == null, "配置已存在，请勿重复添加");
        AdChannelConfig adChannelConfig = BeanConvertUtils.convertBean(adChannelConfigVO, AdChannelConfig.class);
        adChannelConfig.setCreateTime(System.currentTimeMillis());
        adChannelConfig.setUpdateTime(adChannelConfig.getCreateTime());
        if (StringUtils.isBlank(adChannelConfigVO.getAppId())) {
            adChannelConfig.setAppId("lofter");
        }
        delegate.insertValue(adChannelConfig);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = delegate.deleteById(id);
        return count == 1;
    }

    public boolean update(AdChannelConfigVO adChannelConfigVO) {
        AssertUtils.isTrue(adChannelConfigVO != null && adChannelConfigVO.getId() != null, "id missing");
        AdChannelConfig adChannelConfig = delegate.selectById(adChannelConfigVO.getId());
        AssertUtils.notNull(adChannelConfig, "id illegal");
        BeanUtils.copyNonNullProperties(adChannelConfigVO, adChannelConfig);
        adChannelConfig.setUpdateTime(System.currentTimeMillis());
        delegate.updateValue(adChannelConfig);
        return true;
    }

    private List<AdChannelConfigVO> populate2VOList(List<AdChannelConfig> list) {
        return BeanConvertUtils.convertList(list, AdChannelConfigVO.class);
    }


}
