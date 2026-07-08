
package com.netease.lofter.tango.impl.service.trade.activity;

import com.netease.lofter.tango.impl.delegate.trade.activity.PaidContentActivityDelegate;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivity;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.lofter.tango.impl.web.query.trade.activity.PaidContentActivityQuery;
import com.netease.lofter.tango.impl.web.vo.trade.activity.PaidContentActivityVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlUpdate;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


@Service
public class PaidContentActivityService {

    @Autowired
    private PaidContentActivityDelegate paidContentActivityDelegate;

    public PageResult<PaidContentActivityVO> listByQuery(PaidContentActivityQuery query) {
        PageResult<PaidContentActivityVO> pageResult = new PageResult<>(query.getPage());
        PageDO<PaidContentActivity> pageDO = paidContentActivityDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getName(),
            query.getActivityCode(),
            query.getParentActId(),
            query.getImg(),
            query.getType(),
            query.getStartTime(),
            query.getEndTime(),
            query.getDbCreateTime(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(PaidContentActivityVO paidContentActivityVO) {
        PaidContentActivity paidContentActivity = BeanConvertUtils.convertBean(paidContentActivityVO, PaidContentActivity.class);
        paidContentActivity.setDbCreateTime(new Date(System.currentTimeMillis()));
        paidContentActivity.setStatus((byte)0);
        paidContentActivityDelegate.insertValue(paidContentActivity);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = paidContentActivityDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(PaidContentActivityVO paidContentActivityVO) {
        AssertUtils.isTrue(paidContentActivityVO != null && paidContentActivityVO.getId() != null, "id missing");
        PaidContentActivity paidContentActivity = paidContentActivityDelegate.selectById(paidContentActivityVO.getId());
        AssertUtils.notNull(paidContentActivity, "id illegal");
        BeanUtils.copyNonNullProperties(paidContentActivityVO, paidContentActivity);
        paidContentActivityDelegate.updateValue(paidContentActivity);

        if (paidContentActivity.getType() == -1) {
            SqlUpdate sqlUpdate = new SqlUpdate()
                    .setLiteral("activityCode", "'" + paidContentActivityVO.getActivityCode() + "'")
                    .whenEquals("parentActId", paidContentActivity.getId());
            paidContentActivityDelegate.update(sqlUpdate, null);
        }
        return true;
    }

    private List<PaidContentActivityVO> populate2VOList(List<PaidContentActivity> list) {
        return BeanConvertUtils.convertList(list, PaidContentActivityVO.class);
    }


}
