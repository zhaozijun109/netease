
package com.netease.lofter.tango.impl.service.trade.exchangecoupon;

import com.netease.lofter.tango.impl.delegate.trade.activity.PaidContentActivityRewardMapperDelegate;
import com.netease.lofter.tango.impl.delegate.trade.exchangecoupon.TradeExchangeCouponCrowdMapperDelegate;
import com.netease.lofter.tango.impl.delegate.trade.exchangecoupon.TradeExchangeCouponSKUMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityReward;
import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCouponCrowd;
import com.netease.lofter.tango.impl.entity.trade.exchangecoupon.TradeExchangeCouponSKU;
import com.netease.lofter.tango.impl.web.query.trade.exchangecoupon.TradeExchangeCouponSKUQuery;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.ExchangeCouponVO;
import com.netease.lofter.tango.impl.web.vo.trade.exchangecoupon.TradeExchangeCouponSKUVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.core.NumberUtil;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlUpdate;
import com.netease.yaolu.lofter.core.util.JsonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class TradeExchangeCouponSKUService {

    @Autowired
    private TradeExchangeCouponSKUMapperDelegate tradeExchangeCouponSKUDelegate;
    @Autowired
    private PaidContentActivityRewardMapperDelegate rewardMapperDelegate;
    @Autowired
    private TradeExchangeCouponCrowdMapperDelegate exchangeCouponCrowdMapperDelegate;

    public PageResult<TradeExchangeCouponSKUVO> queryActSkus(TradeExchangeCouponSKUQuery query) {
        PageResult<TradeExchangeCouponSKUVO> pageResult = new PageResult<>(query.getPage());

        // 先查出奖品
        PageDO<PaidContentActivityReward> rewards = rewardMapperDelegate.queryByActId(query.getActivityId(), PaidContentActivityReward.TYPE_EXCHANGE_COUPON_PACK, query.getOffset(), query.getLimit());
        List<Long> targetIds = rewards.getList().stream().map(PaidContentActivityReward::getTargetId).collect(Collectors.toList());

        // 查出sku
        List<TradeExchangeCouponSKU> skus = tradeExchangeCouponSKUDelegate.selectByIds(targetIds);
        PageDO<TradeExchangeCouponSKU> pageDO = new PageDO<>(rewards.getTotal(), skus);
        PageResult<TradeExchangeCouponSKUVO> result = pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));

        // 装配
        {
            List<TradeExchangeCouponSKUVO> list = result.getRecords();
            List<Long> ids = list.stream().map(TradeExchangeCouponSKUVO::getId).collect(Collectors.toList());

            Map<Long, PaidContentActivityReward> activityRewardMap = rewardMapperDelegate.getByTargetIds(ids, PaidContentActivityReward.TYPE_EXCHANGE_COUPON_PACK).stream().collect(Collectors.toMap(PaidContentActivityReward::getTargetId, Function.identity()));
            Map<Long, TradeExchangeCouponCrowd> couponCrowdMap = exchangeCouponCrowdMapperDelegate.getBySkuIds(ids).stream().collect(Collectors.toMap(TradeExchangeCouponCrowd::getSkuId, Function.identity()));

            list.forEach(vo -> {
                vo.setExchangeCoupons(JsonUtils.parseToList(vo.getSubProducts(), ExchangeCouponVO.class));

                vo.setGiveawayCoupons(JsonUtils.parseToList(vo.getGiveawayProducts(), ExchangeCouponVO.class));

                TradeExchangeCouponCrowd couponCrowd = couponCrowdMap.get(vo.getId());
                vo.setCrowdId(couponCrowd == null ? 0L : couponCrowd.getCrowdId());

                PaidContentActivityReward activityReward = activityRewardMap.get(vo.getId());
                vo.setActivityId(activityReward.getActId());
            });
        }
        return result;
    }

    public PageResult<TradeExchangeCouponSKUVO> listByQuery(TradeExchangeCouponSKUQuery query) {
        PageResult<TradeExchangeCouponSKUVO> pageResult = new PageResult<>(query.getPage());
        PageDO<TradeExchangeCouponSKU> pageDO = tradeExchangeCouponSKUDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
                query.getId(),
                query.getProductId(),
                query.getType(),
                query.getName(),
                query.getImg(),
                query.getMarketPrice(),
                query.getDiscountPrice(),
                query.getDiscountText(),
                query.getPlatform(),
                query.getStatus(),
                query.getSubProducts(),
                query.getCreateTime(),
                query.getOffset(), query.getLimit());
        PageResult<TradeExchangeCouponSKUVO> result = pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
        List<TradeExchangeCouponSKUVO> list = result.getRecords();
        list.forEach(vo -> {
            vo.setExchangeCoupons(JsonUtils.parseToList(vo.getSubProducts(), ExchangeCouponVO.class));
            vo.setGiveawayCoupons(JsonUtils.parseToList(vo.getGiveawayProducts(), ExchangeCouponVO.class));
        });

        return result;
    }


    public boolean  add(TradeExchangeCouponSKUVO tradeExchangeCouponSKUVO) {
        TradeExchangeCouponSKU tradeExchangeCouponSKU = BeanConvertUtils.convertBean(tradeExchangeCouponSKUVO, TradeExchangeCouponSKU.class);
        tradeExchangeCouponSKU.setCreateTime(System.currentTimeMillis());
        tradeExchangeCouponSKU.setSubProducts(JsonUtils.toJsonString(tradeExchangeCouponSKUVO.getExchangeCoupons()));
        tradeExchangeCouponSKU.setStatus(0);
        tradeExchangeCouponSKU.setGiveawayProducts(JsonUtils.toJsonString(tradeExchangeCouponSKUVO.getGiveawayCoupons()));

        fillDefaultValue(tradeExchangeCouponSKUVO, tradeExchangeCouponSKU);
        int hist = tradeExchangeCouponSKUDelegate.insertValue(tradeExchangeCouponSKU);

        if (hist > 0 && (NumberUtil.in(tradeExchangeCouponSKU.getScene(),
                TradeExchangeCouponSKU.SCENE_ACT,
                TradeExchangeCouponSKU.SCENE_COUPON_CARD,
                TradeExchangeCouponSKU.SCENE_IP_CARD,
                TradeExchangeCouponSKU.SCENE_IP_GROUP_CARD,
                TradeExchangeCouponSKU.SCENE_IP_SINGLE_CARD,
                TradeExchangeCouponSKU.SCENE_IP_CARD_EXCHANGE))) {

            if (tradeExchangeCouponSKUVO.getCrowdId()!= null &&  tradeExchangeCouponSKUVO.getCrowdId() > 0) {
                TradeExchangeCouponCrowd couponCrowd = new TradeExchangeCouponCrowd();
                couponCrowd.setSkuId(tradeExchangeCouponSKU.getId());
                couponCrowd.setCrowdId(tradeExchangeCouponSKUVO.getCrowdId());
                couponCrowd.setCreateTime(System.currentTimeMillis());
                exchangeCouponCrowdMapperDelegate.insertValue(couponCrowd);
            }

            PaidContentActivityReward reward = new PaidContentActivityReward();
            reward.setActId(tradeExchangeCouponSKUVO.getActivityId());
            reward.setName(tradeExchangeCouponSKUVO.getName());
            reward.setTaskId(0L);
            reward.setImg("");
            reward.setType(PaidContentActivityReward.TYPE_EXCHANGE_COUPON_PACK);
            reward.setStatus(0);
            reward.setCount(1);
            reward.setTargetId(tradeExchangeCouponSKU.getId());
            reward.setRewardSchema("");
            reward.setTip("");
            reward.setMessage("");
            reward.setRewardRank(0);
            rewardMapperDelegate.insertValue(reward);
        }
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = tradeExchangeCouponSKUDelegate.deleteById(id);

        exchangeCouponCrowdMapperDelegate.deleteBySkuId(id);
        rewardMapperDelegate.deleteByTargetId(id);

        return count == 1;
    }

    public boolean updateGrantStrategy(long id, String strategy) {
        TradeExchangeCouponSKU tradeExchangeCouponSKU = tradeExchangeCouponSKUDelegate.selectById(id);
        AssertUtils.notNull(tradeExchangeCouponSKU, "id illegal");

        tradeExchangeCouponSKU.setGrantStrategy(strategy);
        int hist = tradeExchangeCouponSKUDelegate.updateValue(tradeExchangeCouponSKU);
        return hist == 1;
    }

    public boolean updateShowCondition(long id, String showCondition) {
        TradeExchangeCouponSKU tradeExchangeCouponSKU = tradeExchangeCouponSKUDelegate.selectById(id);
        AssertUtils.notNull(tradeExchangeCouponSKU, "id illegal");

        tradeExchangeCouponSKU.setShowCondition(showCondition);
        int hist = tradeExchangeCouponSKUDelegate.updateValue(tradeExchangeCouponSKU);
        return hist == 1;
    }

    public boolean updateSettleStrategy(long id, String strategy) {
        TradeExchangeCouponSKU tradeExchangeCouponSKU = tradeExchangeCouponSKUDelegate.selectById(id);
        AssertUtils.notNull(tradeExchangeCouponSKU, "id illegal");

        tradeExchangeCouponSKU.setSettleStrategy(strategy);
        int hist = tradeExchangeCouponSKUDelegate.updateValue(tradeExchangeCouponSKU);
        return hist == 1;
    }


    public boolean update(TradeExchangeCouponSKUVO tradeExchangeCouponSKUVO) {
        AssertUtils.isTrue(tradeExchangeCouponSKUVO != null && tradeExchangeCouponSKUVO.getId() != null, "id missing");
        TradeExchangeCouponSKU tradeExchangeCouponSKU = tradeExchangeCouponSKUDelegate.selectById(tradeExchangeCouponSKUVO.getId());
        tradeExchangeCouponSKU.setSubProducts(JsonUtils.toJsonString(tradeExchangeCouponSKUVO.getExchangeCoupons()));
        tradeExchangeCouponSKU.setGiveawayProducts(JsonUtils.toJsonString(tradeExchangeCouponSKUVO.getGiveawayCoupons()));
        AssertUtils.notNull(tradeExchangeCouponSKU, "id illegal");
        BeanUtils.copyNonNullProperties(tradeExchangeCouponSKUVO, tradeExchangeCouponSKU);

        fillDefaultValue(tradeExchangeCouponSKUVO, tradeExchangeCouponSKU);

        int hist = tradeExchangeCouponSKUDelegate.updateValue(tradeExchangeCouponSKU);
        if (hist > 0 && (NumberUtil.in(tradeExchangeCouponSKU.getScene(),
                TradeExchangeCouponSKU.SCENE_ACT,
                TradeExchangeCouponSKU.SCENE_COUPON_CARD,
                TradeExchangeCouponSKU.SCENE_IP_CARD,
                TradeExchangeCouponSKU.SCENE_IP_GROUP_CARD,
                TradeExchangeCouponSKU.SCENE_IP_SINGLE_CARD))) {
            exchangeCouponCrowdMapperDelegate.deleteBySkuId(tradeExchangeCouponSKU.getId());
            TradeExchangeCouponCrowd couponCrowd = new TradeExchangeCouponCrowd();
            couponCrowd.setSkuId(tradeExchangeCouponSKU.getId());
            couponCrowd.setCrowdId(tradeExchangeCouponSKUVO.getCrowdId());
            couponCrowd.setCreateTime(System.currentTimeMillis());

            exchangeCouponCrowdMapperDelegate.insertValue(couponCrowd);

            PaidContentActivityReward reward = new PaidContentActivityReward();
            reward.setActId(tradeExchangeCouponSKUVO.getActivityId());
            reward.setName(tradeExchangeCouponSKUVO.getName());
            reward.setMessage(tradeExchangeCouponSKUVO.getMessage());
            SqlUpdate sqlUpdateReward = new SqlUpdate().setFields("actId", "message", "name")
                    .whenEquals("targetId", tradeExchangeCouponSKU.getId());
            rewardMapperDelegate.update(sqlUpdateReward, reward);
        }
        return true;
    }

    private List<TradeExchangeCouponSKUVO> populate2VOList(List<TradeExchangeCouponSKU> list) {
        return list.stream().map(s -> {
            TradeExchangeCouponSKUVO vo = BeanConvertUtils.convertBean(s, TradeExchangeCouponSKUVO.class);
            vo.setExchangeCoupons(JsonUtils.parseToList(s.getSubProducts(), ExchangeCouponVO.class));
            return vo;
        }).collect(Collectors.toList());
    }

    private void fillDefaultValue(TradeExchangeCouponSKUVO tradeExchangeCouponSKUVO, TradeExchangeCouponSKU tradeExchangeCouponSKU){
        if (StringUtils.isEmpty(tradeExchangeCouponSKU.getGiveawayPromotion())) {
            tradeExchangeCouponSKU.setGiveawayPromotion("");
        }

        if (CollectionUtils.isEmpty(tradeExchangeCouponSKUVO.getExchangeCoupons())) {
            tradeExchangeCouponSKU.setSubProducts("[]");
            tradeExchangeCouponSKU.setGiveawayPromotion("");
            tradeExchangeCouponSKU.setGiveawayAmount(BigDecimal.ZERO);
        }

        if (CollectionUtils.isEmpty(tradeExchangeCouponSKUVO.getGiveawayCoupons())) {
            tradeExchangeCouponSKU.setGiveawayProducts("[]");
            tradeExchangeCouponSKU.setGiveawayPromotion("");
            tradeExchangeCouponSKU.setGiveawayAmount(BigDecimal.ZERO);
        }

        if(tradeExchangeCouponSKU.getType() == 0 && tradeExchangeCouponSKU.getScene() <= 0) {
            tradeExchangeCouponSKU.setScene(TradeExchangeCouponSKU.SCENE_ACT);
        }
        if (tradeExchangeCouponSKU.getType() == 1 && tradeExchangeCouponSKU.getScene() <= 0) {
            tradeExchangeCouponSKU.setScene(TradeExchangeCouponSKU.SCENE_COUPON_CARD);
        }
        if (tradeExchangeCouponSKU.getType() == 2 && tradeExchangeCouponSKU.getScene() <= 0) {
            tradeExchangeCouponSKU.setScene(TradeExchangeCouponSKU.SCENE_IP_CARD);
        }
        if (tradeExchangeCouponSKU.getType() == 3 && tradeExchangeCouponSKU.getScene() <= 0) {
            tradeExchangeCouponSKU.setScene(TradeExchangeCouponSKU.SCENE_IP_SINGLE_CARD);
        }

        if (tradeExchangeCouponSKU.getType() == 4 && tradeExchangeCouponSKU.getScene() <= 0) {
            tradeExchangeCouponSKU.setScene(TradeExchangeCouponSKU.SCENE_IP_GROUP_CARD);
        }

        if (tradeExchangeCouponSKU.getType() == 5 && tradeExchangeCouponSKU.getScene() <= 0) {
            tradeExchangeCouponSKU.setScene(TradeExchangeCouponSKU.SCENE_IP_CARD_EXCHANGE);
        }

    }

}
