
package com.netease.lofter.tango.impl.service.trade.activity;

import com.netease.lofter.tango.impl.delegate.trade.activity.PaidContentActivityRewardMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityReward;
import com.netease.lofter.tango.impl.web.query.trade.activity.PaidContentActivityRewardQuery;
import com.netease.lofter.tango.impl.web.vo.trade.activity.PaidContentActivityRewardVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityReward.TYPE_SLOT_COUPON;


@Service
public class PaidContentActivityRewardService {

    @Autowired
    private PaidContentActivityRewardMapperDelegate paidContentActivityRewardDelegate;

    public PageResult<PaidContentActivityRewardVO> listByQuery(PaidContentActivityRewardQuery query) {
        if ("generateSlotCoupon".equals(query.getScene())) {
            query.setType(TYPE_SLOT_COUPON);
        }
        PageResult<PaidContentActivityRewardVO> pageResult = new PageResult<>(query.getPage());
        PageDO<PaidContentActivityReward> pageDO = paidContentActivityRewardDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getName(),
            query.getActId(),
            query.getTaskId(),
            query.getImg(),
            query.getType(),
            query.getStatus(),
            query.getCount(),
            query.getTargetId(),
            query.getRewardSchema(),
            query.getTip(),
            query.getMessage(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(PaidContentActivityRewardVO paidContentActivityRewardVO) {
        PaidContentActivityReward paidContentActivityReward = BeanConvertUtils.convertBean(paidContentActivityRewardVO, PaidContentActivityReward.class);
        paidContentActivityReward.setStatus(0);
        paidContentActivityReward.setTaskId(0L);
        paidContentActivityReward.setRewardRank(0);
        paidContentActivityReward.setRewardSchema("");
        if(StringUtils.isEmpty(paidContentActivityReward.getMessage())) {
            paidContentActivityReward.setMessage("");
        }

        paidContentActivityRewardDelegate.insertValue(paidContentActivityReward);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = paidContentActivityRewardDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(PaidContentActivityRewardVO paidContentActivityRewardVO) {
        AssertUtils.isTrue(paidContentActivityRewardVO != null && paidContentActivityRewardVO.getId() != null, "id missing");
        PaidContentActivityReward paidContentActivityReward = paidContentActivityRewardDelegate.selectById(paidContentActivityRewardVO.getId());
        AssertUtils.notNull(paidContentActivityReward, "id illegal");
        BeanUtils.copyNonNullProperties(paidContentActivityRewardVO, paidContentActivityReward);
        paidContentActivityReward.setStatus(0);
        paidContentActivityReward.setTaskId(0L);
        paidContentActivityReward.setRewardRank(0);
        paidContentActivityReward.setRewardSchema("");
        if(StringUtils.isEmpty(paidContentActivityReward.getMessage())) {
            paidContentActivityReward.setMessage("");
        }

        paidContentActivityRewardDelegate.updateValue(paidContentActivityReward);
        return true;
    }

    private List<PaidContentActivityRewardVO> populate2VOList(List<PaidContentActivityReward> list) {
        return BeanConvertUtils.convertList(list, PaidContentActivityRewardVO.class);
    }


}
