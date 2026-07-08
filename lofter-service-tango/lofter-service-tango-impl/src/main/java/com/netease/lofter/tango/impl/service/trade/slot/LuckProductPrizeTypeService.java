
package com.netease.lofter.tango.impl.service.trade.slot;

import com.netease.lofter.tango.impl.delegate.trade.slot.LuckProductPrizeTypeDelegate;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckProductPrizeType;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.lofter.tango.impl.web.query.trade.slot.LuckProductPrizeTypeQuery;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckProductPrizeTypeVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class LuckProductPrizeTypeService {

    @Autowired
    private LuckProductPrizeTypeDelegate luckProductPrizeTypeDelegate;

    public PageResult<LuckProductPrizeTypeVO> listByQuery(LuckProductPrizeTypeQuery query) {
        PageResult<LuckProductPrizeTypeVO> pageResult = new PageResult<>(query.getPage());
        PageDO<LuckProductPrizeType> pageDO = luckProductPrizeTypeDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getAppKey(),
            query.getType(),
            query.getName(),
            query.getCustomInfo(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(LuckProductPrizeTypeVO luckProductPrizeTypeVO) {
        LuckProductPrizeType luckProductPrizeType = BeanConvertUtils.convertBean(luckProductPrizeTypeVO, LuckProductPrizeType.class);
        luckProductPrizeType.setCreateTime(System.currentTimeMillis());
        luckProductPrizeType.setUpdateTime(luckProductPrizeType.getCreateTime());
        luckProductPrizeTypeDelegate.insertValue(luckProductPrizeType);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = luckProductPrizeTypeDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(LuckProductPrizeTypeVO luckProductPrizeTypeVO) {
        AssertUtils.isTrue(luckProductPrizeTypeVO != null && luckProductPrizeTypeVO.getId() != null, "id missing");
        LuckProductPrizeType luckProductPrizeType = luckProductPrizeTypeDelegate.selectById(luckProductPrizeTypeVO.getId());
        AssertUtils.notNull(luckProductPrizeType, "id illegal");
        BeanUtils.copyNonNullProperties(luckProductPrizeTypeVO, luckProductPrizeType);
        luckProductPrizeType.setUpdateTime(System.currentTimeMillis());
        luckProductPrizeTypeDelegate.updateValue(luckProductPrizeType);
        return true;
    }

    private List<LuckProductPrizeTypeVO> populate2VOList(List<LuckProductPrizeType> list) {
        return BeanConvertUtils.convertList(list, LuckProductPrizeTypeVO.class);
    }


}
