
package com.netease.lofter.tango.impl.service.trade.activity;

import com.netease.lofter.tango.impl.delegate.trade.activity.PaidContentActivityDelegate;
import com.netease.lofter.tango.impl.delegate.trade.activity.PaidContentActivitySlotBooxMapperDelegate;
import com.netease.lofter.tango.impl.delegate.trade.slot.LuckActivityDelegate;
import com.netease.lofter.tango.impl.delegate.trade.slot.LuckPrizeDelegate;
import com.netease.lofter.tango.impl.delegate.trade.slot.LuckPrizeProbabilityStrategyDelegate;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivity;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivitySlotBoox;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckActivity;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrize;
import com.netease.lofter.tango.impl.service.trade.slot.LuckActivityService;
import com.netease.lofter.tango.impl.web.query.trade.activity.PaidContentActivitySlotBooxQuery;
import com.netease.lofter.tango.impl.web.vo.trade.activity.PaidContentActivitySlotBooxVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlUpdate;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class PaidContentActivitySlotBooxService {

    @Autowired
    private PaidContentActivitySlotBooxMapperDelegate paidContentActivitySlotBooxDelegate;
    @Autowired
    private PaidContentActivityDelegate paidContentActivityDelegate;
    @Autowired
    private LuckActivityDelegate luckActivityDelegate;
    @Autowired
    private LuckPrizeDelegate prizeDelegate;
    @Autowired
    private LuckPrizeProbabilityStrategyDelegate probabilityStrategyDelegate;

    public PageResult<PaidContentActivitySlotBooxVO> listByQuery(PaidContentActivitySlotBooxQuery query) {
        PageResult<PaidContentActivitySlotBooxVO> pageResult = new PageResult<>(query.getPage());
        PageDO<PaidContentActivitySlotBoox> pageDO = paidContentActivitySlotBooxDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getName(),
            query.getActId(),
            query.getLootboxCode(),
            query.getStartTime(),
            query.getEndTime(),
            query.getCreateTime(),
            query.getOffset(), query.getLimit());

        List<LuckActivity> activities = luckActivityDelegate.getByActivityIds(pageDO.getList().stream().map(PaidContentActivitySlotBoox::getLootboxCode).collect(Collectors.toList()));
        Map<String, LuckActivity> activityMap = activities.stream().collect(Collectors.toMap(LuckActivity::getActivityId, a -> a));

        PageResult<PaidContentActivitySlotBooxVO> result = pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
        result.getRecords().forEach(p -> {
            LuckActivity activity = activityMap.get(p.getLootboxCode());
            if(activity == null) {
                return;
            }

            p.setDailyInitChanceCount(activity.getDailyInitChanceCount());
            p.setBingoCount(activity.getBingoCount());
            p.setFakeBingoCount(activity.getFakeBingoCount());
            p.setJoinCount(activity.getJoinCount());
            p.setFakeJoinCount(activity.getFakeJoinCount());
            p.setFakeJoinCountRateMax(activity.getFakeJoinCountRateMax());
            p.setFakeJoinCountRateMin(activity.getFakeJoinCountRateMin());
            p.setDefaultInitChanceCount(activity.getDefaultInitChanceCount());
            p.setName(activity.getName());
        });

       return result;
    }


    public boolean add(PaidContentActivitySlotBooxVO paidContentActivitySlotBooxVO) {
        PaidContentActivitySlotBoox paidContentActivitySlotBoox = BeanConvertUtils.convertBean(paidContentActivitySlotBooxVO, PaidContentActivitySlotBoox.class);
        paidContentActivitySlotBoox.setCreateTime(System.currentTimeMillis());
        paidContentActivitySlotBooxDelegate.insertValue(paidContentActivitySlotBoox);
        paidContentActivitySlotBooxVO.setId(paidContentActivitySlotBoox.getId());
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = paidContentActivitySlotBooxDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(PaidContentActivitySlotBooxVO paidContentActivitySlotBooxVO) {
        LuckActivity slotBoox = luckActivityDelegate.selectById(paidContentActivitySlotBooxVO.getId());

        AssertUtils.notNull(slotBoox, "slotBoox is missing");

        SqlUpdate updatePrizeAct = new SqlUpdate()
                .setLiteral("startTime", paidContentActivitySlotBooxVO.getStartTime())
                .setLiteral("endTime", paidContentActivitySlotBooxVO.getEndTime())
                .setLiteral("activityId", "'" + paidContentActivitySlotBooxVO.getActivityId() + "'")
                .whenEquals("activityId", slotBoox.getActivityId());
        luckActivityDelegate.update(updatePrizeAct,null);

        SqlUpdate updateLootBox = new SqlUpdate()
                .setLiteral("startTime", paidContentActivitySlotBooxVO.getStartTime())
                .setLiteral("endTime", paidContentActivitySlotBooxVO.getEndTime())
                .setLiteral("lootboxCode", "'" + paidContentActivitySlotBooxVO.getActivityId()+ "'")
                .whenEquals("lootboxCode", slotBoox.getActivityId());
        paidContentActivitySlotBooxDelegate.update(updateLootBox,null);

        if (!StringUtils.equals(paidContentActivitySlotBooxVO.getLootboxCode(), slotBoox.getActivityId())) {
            SqlUpdate updatePrize = new SqlUpdate()
                    .setLiteral("activityId", "'" + paidContentActivitySlotBooxVO.getActivityId() + "'")
                    .whenEquals("activityId", slotBoox.getActivityId());
            prizeDelegate.update(updatePrize, null);
            probabilityStrategyDelegate.update(updatePrize, null);
        }
        return true;
    }

    public boolean copy(List<Long> ids){
        AssertUtils.isTrue(CollectionUtils.isNotEmpty(ids), "ids missing");
        List<PaidContentActivitySlotBoox> slotBoxes = paidContentActivitySlotBooxDelegate.selectByIds(ids);
        slotBoxes.forEach(slotBox -> {
            long actId = slotBox.getActId();
            PaidContentActivity activity = paidContentActivityDelegate.selectById(actId);
            AssertUtils.isTrue(activity != null, "activity missing");

            PaidContentActivitySlotBoox newBox = new PaidContentActivitySlotBoox();
            BeanUtils.copyNonNullProperties(slotBox, newBox);

            // step1 复制活动奖池
            String lootBoxCode = activity.getActivityCode() + "_" + slotBox.getId();
            newBox.setId(null);
            newBox.setName(lootBoxCode);
            newBox.setLootboxCode(lootBoxCode);
            newBox.setStartTime(slotBox.getStartTime());
            newBox.setEndTime(newBox.getStartTime() + slotBox.getEndTime() - slotBox.getStartTime());
            newBox.setCreateTime(System.currentTimeMillis());
            paidContentActivitySlotBooxDelegate.insertValue(newBox);

            // step2 复制抽奖平台活动
            LuckActivity luckActivity = luckActivityDelegate.getByActivityId(lootBoxCode);
            if(luckActivity != null) {
                luckActivity.setId(null);
                luckActivity.setName(lootBoxCode);
                luckActivity.setCreateTime(System.currentTimeMillis());
                luckActivity.setActivityId(lootBoxCode);
                luckActivity.setStartTime(luckActivity.getStartTime());
                luckActivity.setEndTime(luckActivity.getStartTime() + luckActivity.getEndTime() - luckActivity.getStartTime());
                luckActivityDelegate.insertValue(luckActivity);

                List<LuckPrize> prizes =  new ArrayList<>();
            }
        });
        return true;
    }

    private List<PaidContentActivitySlotBooxVO> populate2VOList(List<PaidContentActivitySlotBoox> list) {
        return BeanConvertUtils.convertList(list, PaidContentActivitySlotBooxVO.class);
    }


}
