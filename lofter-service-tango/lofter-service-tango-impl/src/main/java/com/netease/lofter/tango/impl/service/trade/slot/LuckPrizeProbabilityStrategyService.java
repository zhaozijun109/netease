
package com.netease.lofter.tango.impl.service.trade.slot;

import com.netease.lofter.tango.impl.delegate.trade.slot.LuckActivityDelegate;
import com.netease.lofter.tango.impl.delegate.trade.slot.LuckPrizeDelegate;
import com.netease.lofter.tango.impl.delegate.trade.slot.LuckPrizeProbabilityStrategyDelegate;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckActivity;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrize;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrizeProbabilityStrategy;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.mapper.trade.slot.LuckPrizeMapper;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.lofter.tango.impl.web.query.trade.slot.LuckPrizeProbabilityStrategyQuery;
import com.netease.lofter.tango.impl.web.vo.trade.slot.LuckPrizeProbabilityStrategyVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import com.netease.yaolu.lofter.core.util.JsonUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
public class LuckPrizeProbabilityStrategyService {

    @Autowired
    private LuckPrizeProbabilityStrategyDelegate luckPrizeProbabilityStrategyDelegate;
    @Autowired
    private LuckActivityDelegate luckActivityDelegate;
    @Autowired
    private LuckPrizeDelegate luckPrizeDelegate;;

    public LuckPrizeProbabilityStrategyVO getDetail(String activityId) {
        LuckPrizeProbabilityStrategyVO vo = BeanConvertUtils.convertBean(luckPrizeProbabilityStrategyDelegate.selectById(activityId), LuckPrizeProbabilityStrategyVO.class);
        {
            List<Long> prizeIds = new ArrayList<>();

            List<LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO> prizeList = JsonUtils.parseToList(vo.getProbabilityStrategy());
            prizeIds.addAll(prizeList.stream().map(LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO::getPrizeId).collect(Collectors.toList()));

            List<LuckPrize> prizes = luckPrizeDelegate.selectByIds(prizeIds);
            Map<Long, LuckPrize> prizeMap = prizes.stream().collect(Collectors.toMap(LuckPrize::getId, Function.identity()));

            prizeList.forEach( prize -> {
                LuckPrize l = prizeMap.get(prize.getPrizeId());
                if(l == null) {
                    return;
                }
                prize.setRewardName(l.getName());
            });

            vo.setPrizeList(prizeList);
        }
        return vo;
    }

    public PageResult<LuckPrizeProbabilityStrategyVO> listByQuery(LuckPrizeProbabilityStrategyQuery query) {
        PageResult<LuckPrizeProbabilityStrategyVO> pageResult = new PageResult<>(query.getPage());
        PageDO<LuckPrizeProbabilityStrategy> pageDO = luckPrizeProbabilityStrategyDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
            query.getId(),
            query.getAppKey(),
            query.getActivityId(),
            query.getStartTime(),
            query.getEndTime(),
            query.getProbabilityStrategy(),
            query.getOffset(), query.getLimit());

        PageResult<LuckPrizeProbabilityStrategyVO> res = pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
        List<Long> prizeIds = new ArrayList<>();
        res.getRecords().forEach(vo -> {
            List<LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO> prizeList = JsonUtils.parseToList(vo.getProbabilityStrategy(),LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO.class);
            prizeIds.addAll(prizeList.stream().map(LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO::getPrizeId).collect(Collectors.toList()));
        });

        List<LuckPrize> dbPrizeList = luckPrizeDelegate.selectByIds(prizeIds);
        Map<Long, LuckPrize> prizeMap = dbPrizeList.stream().collect(Collectors.toMap(LuckPrize::getId, Function.identity()));

        res.getRecords().forEach(vo -> {
            List<LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO> prizeList = JsonUtils.parseToList(vo.getProbabilityStrategy(), LuckPrizeProbabilityStrategyVO.LuckPrizeProbabilityVO.class);
            prizeList.forEach( prize -> {
                LuckPrize l = prizeMap.get(prize.getPrizeId());
                if(l == null) {
                    return;
                }
                prize.setRewardName(l.getName());
            });
            vo.setPrizeList(prizeList);
        });

        return res;
    }


    public boolean add(LuckPrizeProbabilityStrategyVO luckPrizeProbabilityStrategyVO) {
        SqlSelect sqlAct = new SqlSelect().whenEquals("activityId", luckPrizeProbabilityStrategyVO.getActivityId());
        LuckActivity activity = luckActivityDelegate.selectOne(sqlAct);
        AssertUtils.isTrue(activity != null, "luckActivity not found");

        SqlSelect sqlStrategy = new SqlSelect().whenEquals("activityId", luckPrizeProbabilityStrategyVO.getActivityId());
        LuckPrizeProbabilityStrategy strategy = luckPrizeProbabilityStrategyDelegate.selectOne(sqlStrategy);
        AssertUtils.isTrue(strategy == null, "最多添加一个中奖策略");

        LuckPrizeProbabilityStrategy luckPrizeProbabilityStrategy = BeanConvertUtils.convertBean(luckPrizeProbabilityStrategyVO, LuckPrizeProbabilityStrategy.class);
        luckPrizeProbabilityStrategy.setAppKey("lofter");
        luckPrizeProbabilityStrategy.setStartTime(activity.getStartTime());
        luckPrizeProbabilityStrategy.setEndTime(activity.getEndTime());
        luckPrizeProbabilityStrategy.setCreateTime(System.currentTimeMillis());
        luckPrizeProbabilityStrategy.setUpdateTime(luckPrizeProbabilityStrategy.getCreateTime());
        luckPrizeProbabilityStrategyDelegate.insertValue(luckPrizeProbabilityStrategy);
        return true;
    }

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = luckPrizeProbabilityStrategyDelegate.deleteById(id);
        return count == 1;
    }

    public boolean update(LuckPrizeProbabilityStrategyVO luckPrizeProbabilityStrategyVO) {
        AssertUtils.isTrue(luckPrizeProbabilityStrategyVO != null && luckPrizeProbabilityStrategyVO.getId() != null, "id missing");
        LuckPrizeProbabilityStrategy luckPrizeProbabilityStrategy = luckPrizeProbabilityStrategyDelegate.selectById(luckPrizeProbabilityStrategyVO.getId());
        AssertUtils.notNull(luckPrizeProbabilityStrategy, "id illegal");
        BeanUtils.copyNonNullProperties(luckPrizeProbabilityStrategyVO, luckPrizeProbabilityStrategy);
        luckPrizeProbabilityStrategy.setUpdateTime(System.currentTimeMillis());
        luckPrizeProbabilityStrategyDelegate.updateValue(luckPrizeProbabilityStrategy);
        return true;
    }

    private List<LuckPrizeProbabilityStrategyVO> populate2VOList(List<LuckPrizeProbabilityStrategy> list) {
        return BeanConvertUtils.convertList(list, LuckPrizeProbabilityStrategyVO.class);
    }


}
