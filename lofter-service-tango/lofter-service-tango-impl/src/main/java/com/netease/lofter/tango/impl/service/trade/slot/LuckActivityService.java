
package com.netease.lofter.tango.impl.service.trade.slot;

import com.netease.lofter.tango.impl.delegate.trade.slot.LuckActivityDelegate;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckActivity;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrize;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.lofter.tango.impl.web.query.trade.slot.LuckActivityQuery;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckActivityVO;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckPrizeVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;


@Service
public class LuckActivityService {

    @Autowired
    private LuckActivityDelegate luckActivityDelegate;

    public LuckActivityVO getDetail(String activityId) {
        SqlSelect sqlSelect = new SqlSelect().whenEquals("activityId", activityId).andEquals("appKey", "lofter");
        LuckActivity luckPrize = luckActivityDelegate.selectOne(sqlSelect);
        return BeanConvertUtils.convertBean(luckPrize, LuckActivityVO.class);
    }

    public PageResult<LuckActivityVO> listByQuery(LuckActivityQuery query) {
        PageResult<LuckActivityVO> pageResult = new PageResult<>(query.getPage());
        PageDO<LuckActivity> pageDO = luckActivityDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getAppKey(),
            query.getActivityId(),
            query.getName(),
            query.getStartTime(),
            query.getEndTime(),
            query.getDailyInitChanceCount(),
            query.getDailySlotLimit(),
            query.getUserBingoLimit(),
            query.getBingoCount(),
            query.getFakeBingoCount(),
            query.getJoinCount(),
            query.getFakeJoinCount(),
            query.getFakeJoinCountRateMin(),
            query.getFakeJoinCountRateMax(),
            query.getDefaultInitChanceCount(),
            query.getOffset(), query.getLimit());
       return pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
    }


    public boolean add(LuckActivityVO luckActivityVO) {
        LuckActivity luckActivity = BeanConvertUtils.convertBean(luckActivityVO, LuckActivity.class);
        luckActivity.setCreateTime(System.currentTimeMillis());
        luckActivity.setUpdateTime(luckActivity.getCreateTime());
        luckActivity.setJoinCount(0);
        luckActivityDelegate.insertValue(luckActivity);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = luckActivityDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(LuckActivityVO luckActivityVO) {
        SqlSelect sqlSelect = new SqlSelect().whenEquals("activityId", luckActivityVO.getActivityId());
        LuckActivity luckActivity = luckActivityDelegate.selectOne(sqlSelect);
        AssertUtils.notNull(luckActivity, "update luckActivity fail, luckActivity not found");
        luckActivity.setUpdateTime(System.currentTimeMillis());

        SqlUpdate sqlUpdate = new SqlUpdate()
                .setLiteral("updateTime", System.currentTimeMillis())
                .setLiteral("startTime", luckActivityVO.getStartTime())
                .setLiteral("endTime", luckActivityVO.getEndTime())
                .setLiteral("dailySlotLimit", luckActivityVO.getDailySlotLimit())
                .setLiteral("dailyInitChanceCount", luckActivityVO.getDailyInitChanceCount())
                .setLiteral("dailySlotLimit", luckActivityVO.getDailySlotLimit())
                .setLiteral("userBingoLimit", luckActivityVO.getUserBingoLimit())
                .whenEquals("activityId", luckActivityVO.getActivityId());
        int hist = luckActivityDelegate.update(sqlUpdate, null);
        return hist > 0;
    }

    private List<LuckActivityVO> populate2VOList(List<LuckActivity> list) {
        return BeanConvertUtils.convertList(list, LuckActivityVO.class);
    }


}
