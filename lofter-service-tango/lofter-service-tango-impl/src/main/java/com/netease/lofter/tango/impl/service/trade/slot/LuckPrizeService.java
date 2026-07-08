
package com.netease.lofter.tango.impl.service.trade.slot;

import com.netease.lofter.tango.impl.delegate.trade.activity.PaidContentActivityRewardMapperDelegate;
import com.netease.lofter.tango.impl.delegate.trade.activity.PaidContentActivitySlotBooxMapperDelegate;
import com.netease.lofter.tango.impl.delegate.trade.slot.LuckPrizeDelegate;
import com.netease.lofter.tango.impl.delegate.trade.slot.LuckPrizeProbabilityStrategyDelegate;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityReward;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrize;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrizeProbabilityStrategy;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.lofter.tango.impl.web.query.trade.slot.LuckPrizeQuery;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckPrizeProbabilityStrategyVO;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckPrizeVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlUpdate;
import com.netease.yaolu.lofter.core.util.JsonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class LuckPrizeService {

    @Autowired
    private LuckPrizeDelegate luckPrizeDelegate;
    @Autowired
    private PaidContentActivityRewardMapperDelegate rewardDelegate;
    @Autowired
    private LuckPrizeProbabilityStrategyDelegate probabilityStrategyDelegate;
    @Autowired
    private PaidContentActivitySlotBooxMapperDelegate slotBooxMapperDelegate;

    public LuckPrizeVO getDetail(Long id) {
        SqlSelect sqlSelect = new SqlSelect().whenEquals("id", id);
        LuckPrize luckPrize = luckPrizeDelegate.selectOne(sqlSelect);

        LuckPrize.PrizeDetail detail = JsonUtils.toObject(luckPrize.getCustomInfo(), LuckPrize.PrizeDetail.class);
        PaidContentActivityReward reward = rewardDelegate.selectById(Long.parseLong(detail.getAttributeId()));

        LuckPrizeVO res = BeanConvertUtils.convertBean(luckPrize, LuckPrizeVO.class);
        res.setRewardRank(reward.getRewardRank());
        res.setTip(reward.getTip());
        res.setTargetId(reward.getTargetId());
        res.setRewardSchema(reward.getRewardSchema());
        res.setRewardType(reward.getType());
        res.setGrantCount(reward.getCount());

        return res;
    }

    public PageResult<LuckPrizeVO> listByQuery(LuckPrizeQuery query) {
        PageResult<LuckPrizeVO> pageResult = new PageResult<>(query.getPage());
        PageDO<LuckPrize> pageDO = luckPrizeDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
                query.getId(),
                query.getAppKey(),
                query.getActivityId(),
                query.getName(),
                query.getPicUrl(),
                query.getPrizeType(),
                query.getThirdpartLink(),
                query.getCount(),
                query.getShowFlag(),
                query.getShowCount(),
                query.getIndexId(),
                query.getPrice(),
                query.getDailyBingoLimit(),
                query.getUserBingoLimit(),
                query.getBingoCheckFlag(),
                query.getBingoNoticeContent(),
                query.getBingoCount(),
                query.getCustomInfo(),
                query.getOffset(), query.getLimit());

        PageResult result = pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
        List<LuckPrizeVO> list = result.getRecords();

        List<Long> rewardIds = new ArrayList<>();
        for (LuckPrizeVO luckPrizeVO : list) {
            LuckPrize.PrizeDetail detail = JsonUtils.toObject(luckPrizeVO.getCustomInfo(), LuckPrize.PrizeDetail.class);
            rewardIds.add(Long.parseLong(detail.getAttributeId()));
        }

        if (CollectionUtils.isNotEmpty(rewardIds)) {
            List<PaidContentActivityReward> rewards = rewardDelegate.selectByIds(rewardIds);
            Map<Long, PaidContentActivityReward> rewardMap = rewards.stream().collect(Collectors.toMap(PaidContentActivityReward::getId, Function.identity()));
            for (LuckPrizeVO luckPrizeVO : list) {
                LuckPrize.PrizeDetail detail = JsonUtils.toObject(luckPrizeVO.getCustomInfo(), LuckPrize.PrizeDetail.class);
                PaidContentActivityReward reward = rewardMap.get(Long.parseLong(detail.getAttributeId()));
                luckPrizeVO.setRewardRank(reward.getRewardRank());
                luckPrizeVO.setTip(reward.getTip());
                luckPrizeVO.setTargetId(reward.getTargetId());
                luckPrizeVO.setRewardSchema(reward.getRewardSchema());
                luckPrizeVO.setRewardType(reward.getType());
                luckPrizeVO.setGrantCount(reward.getCount());
            }
        }

        return result;
    }

    public boolean add(LuckPrizeVO luckPrizeVO) {
        PaidContentActivityReward reward = addReward(luckPrizeVO);
        if (reward == null) {
            return false;
        }

        LuckPrize.PrizeDetail detail = new LuckPrize.PrizeDetail();
        detail.setAttributeId(String.valueOf(reward.getId()));

        LuckPrize luckPrize = BeanConvertUtils.convertBean(luckPrizeVO, LuckPrize.class);
        luckPrize.setAppKey("lofter");
        luckPrize.setCreateTime(System.currentTimeMillis());
        luckPrize.setUpdateTime(luckPrize.getCreateTime());
        luckPrize.setCustomInfo(JsonUtils.toJsonString(detail));
        luckPrize.setDailyBingoLimit(-1);
        luckPrize.setBingoCount(0);
        // 抽奖平台通用抽奖类型
        luckPrize.setPrizeType(35);
        int hist = luckPrizeDelegate.insertValue(luckPrize);

        SqlSelect sqlSelect = new SqlSelect().whenEquals("activityId", luckPrizeVO.getActivityId());
        LuckPrizeProbabilityStrategy strategy = probabilityStrategyDelegate.selectOne(sqlSelect);

        if(strategy != null) {
            List<LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO> prizeProbabilityVOS = JsonUtils.parseToList(strategy.getProbabilityStrategy(), LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO.class);
            prizeProbabilityVOS.add(new LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO(luckPrize.getId(), 0));
            strategy.setProbabilityStrategy(JsonUtils.toJsonString(prizeProbabilityVOS));
            probabilityStrategyDelegate.updateValue(strategy);
        }
        return hist > 0;
    }

    public boolean delete(Long prizeId) {
        LuckPrize luckPrize = luckPrizeDelegate.selectById(prizeId);
        AssertUtils.notNull(luckPrize, "id illegal");
        LuckPrize.PrizeDetail detail = JsonUtils.jsonToObj(luckPrize.getCustomInfo(), LuckPrize.PrizeDetail.class);

        SqlSelect sqlSelect = new SqlSelect().whenEquals("activityId", luckPrize.getActivityId());
        LuckPrizeProbabilityStrategy strategy = probabilityStrategyDelegate.selectOne(sqlSelect);

        List<LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO> prizeProbabilityVOS = JsonUtils.parseToList(strategy.getProbabilityStrategy());
        prizeProbabilityVOS.removeIf(vo -> vo.getPrizeId() == prizeId);
        strategy.setProbabilityStrategy(JsonUtils.toJsonString(prizeProbabilityVOS));
        probabilityStrategyDelegate.updateValue(strategy);

        return rewardDelegate.deleteById(detail.getAttributeId()) > 0;
    }

    public boolean update(LuckPrizeVO luckPrizeVO) {
        AssertUtils.isTrue(luckPrizeVO != null && luckPrizeVO.getId() != null, "id missing");
        LuckPrize luckPrize = luckPrizeDelegate.selectById(luckPrizeVO.getId());
        AssertUtils.notNull(luckPrize, "id illegal");
        BeanUtils.copyNonNullProperties(luckPrizeVO, luckPrize);
        luckPrize.setUpdateTime(System.currentTimeMillis());
        luckPrizeDelegate.updateValue(luckPrize);

        updateReward(luckPrizeVO);
        return true;
    }

    private List<LuckPrizeVO> populate2VOList(List<LuckPrize> list) {
        return BeanConvertUtils.convertList(list, LuckPrizeVO.class);
    }

    private PaidContentActivityReward addReward(LuckPrizeVO luckPrizeVO) {
        long actId = slotBooxMapperDelegate.getActIdByLootboxCode(luckPrizeVO.getActivityId());
        AssertUtils.isTrue(actId > 0, "lootbox code illegal");

        PaidContentActivityReward reward = new PaidContentActivityReward();
        reward.setActId(actId);
        reward.setName(luckPrizeVO.getName());
        reward.setTaskId(0L);
        reward.setImg(luckPrizeVO.getPicUrl());
        reward.setType(luckPrizeVO.getRewardType());
        reward.setStatus(0);
        reward.setCount(luckPrizeVO.getGrantCount());
        reward.setTargetId(luckPrizeVO.getTargetId());
        reward.setRewardSchema(luckPrizeVO.getRewardSchema());
        reward.setTip(luckPrizeVO.getTip());
        reward.setMessage(luckPrizeVO.getBingoNoticeContent());
        reward.setRewardRank(luckPrizeVO.getRewardRank());
        int hist = rewardDelegate.insertValue(reward);
        return hist > 0 ? reward : null;
    }

    private int updateReward(LuckPrizeVO luckPrizeVO) {
        long prizeId = luckPrizeVO.getId();
        LuckPrize luckPrize = luckPrizeDelegate.selectById(prizeId);
        AssertUtils.notNull(luckPrize, "id illegal");

        LuckPrize.PrizeDetail detail = JsonUtils.jsonToObj(luckPrize.getCustomInfo(), LuckPrize.PrizeDetail.class);

        PaidContentActivityReward reward = new PaidContentActivityReward();
        reward.setId(Long.parseLong(detail.getAttributeId()));
        reward.setImg(luckPrizeVO.getPicUrl());
        reward.setType(luckPrizeVO.getRewardType());
        reward.setCount(luckPrizeVO.getGrantCount());
        reward.setName(luckPrizeVO.getName());
        reward.setRewardRank(luckPrizeVO.getRewardRank());
        reward.setTargetId(luckPrizeVO.getTargetId());
        reward.setTip(luckPrizeVO.getTip());
        reward.setRewardSchema(luckPrizeVO.getRewardSchema());
        reward.setMessage(luckPrizeVO.getBingoNoticeContent());

        SqlUpdate updateReward = new SqlUpdate().setFields("img","name",
                        "type", "count", "rewardRank", "targetId", "tip", "rewardSchema", "message")
                .whenEquals("id", Long.parseLong(detail.getAttributeId()));
        return rewardDelegate.update(updateReward, reward);
    }
}
