
package com.netease.lofter.tango.impl.service.trade.activity;

import com.google.common.collect.Lists;
import com.netease.lofter.tango.impl.delegate.trade.activity.PaidContentActivityProgressTaskMapperDelegate;
import com.netease.lofter.tango.impl.delegate.trade.activity.PaidContentActivityRewardMapperDelegate;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityProgressTask;
import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityReward;
import com.netease.lofter.tango.impl.web.query.trade.activity.PaidContentActivityProgressTaskQuery;
import com.netease.lofter.tango.impl.web.vo.trade.activity.PaidContentActivityProgressTaskVO;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.util.BeanUtils;

import com.netease.lofter.tango.impl.web.vo.trade.activity.PaidContentActivityRewardVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlDelete;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlUpdate;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class PaidContentActivityProgressTaskService {

    @Autowired
    private PaidContentActivityProgressTaskMapperDelegate paidContentActivityProgressTaskDelegate;
    @Autowired
    private PaidContentActivityRewardMapperDelegate rewardMapperDelegate;

    public PageResult<PaidContentActivityProgressTaskVO> listByQuery(PaidContentActivityProgressTaskQuery query) {
        PageResult<PaidContentActivityProgressTaskVO> pageResult = new PageResult<>(query.getPage());
        PageDO<PaidContentActivityProgressTask> pageDO = paidContentActivityProgressTaskDelegate.listByQuery(query.getCreateTimeBegin(), query.getCreateTimeEnd(),
                query.getId(),
                query.getName(),
                query.getActId(),
                query.getImg(),
                query.getGoal(),
                query.getStatus(),
                query.getEventType(),
                query.getOffset(), query.getLimit());
        PageResult<PaidContentActivityProgressTaskVO> res = pageResult.total(pageDO.getTotal()).list(populate2VOList(pageDO.getList()));
        List<Long> taskIds = res.getRecords().stream().map(PaidContentActivityProgressTaskVO::getId).collect(Collectors.toList());

        Map<Long, List<PaidContentActivityReward>> rewardGroup = rewardMapperDelegate.getTaskRewards(query.getActId(), taskIds);

        res.getRecords().forEach(vo -> {
            List<PaidContentActivityReward> rewards = rewardGroup.get(vo.getId());
            if (CollectionUtils.isNotEmpty(rewards)) {
                List<PaidContentActivityRewardVO> rewardVOS = BeanConvertUtils.convertList(rewards, PaidContentActivityRewardVO.class);
                vo.setRewards(rewardVOS);
                rewardVOS.forEach(PaidContentActivityRewardVO::conventStr);
            }
        });
        return res;
    }


    public boolean add(PaidContentActivityProgressTaskVO paidContentActivityProgressTaskVO) {
        PaidContentActivityProgressTask paidContentActivityProgressTask = BeanConvertUtils.convertBean(paidContentActivityProgressTaskVO, PaidContentActivityProgressTask.class);
        paidContentActivityProgressTask.setStatus(0);
        if(paidContentActivityProgressTask.getEndTime() == null) {
            paidContentActivityProgressTask.setEndTime(0L);
            paidContentActivityProgressTask.setStartTime(0L);
        }
        if (paidContentActivityProgressTask.getTip() == null) {
            paidContentActivityProgressTask.setTip("");
        }
        paidContentActivityProgressTaskDelegate.insertValue(paidContentActivityProgressTask);

        if (CollectionUtils.isNotEmpty(paidContentActivityProgressTaskVO.getRewards())) {
            paidContentActivityProgressTaskVO.getRewards().forEach(PaidContentActivityRewardVO::convertToExt);
            List<PaidContentActivityReward> rewards = BeanConvertUtils.convertList(paidContentActivityProgressTaskVO.getRewards(), PaidContentActivityReward.class);
            rewards.forEach(reward -> {
                reward.setActId(paidContentActivityProgressTask.getActId());
                reward.setStatus(0);
                reward.setTaskId(paidContentActivityProgressTask.getId());
                reward.setRewardRank(0);
                reward.setRewardSchema("");
                reward.setTip("");
                if(StringUtils.isEmpty(reward.getMessage())) {
                    reward.setMessage("");
                }
            });
            rewardMapperDelegate.insertValues(rewards);
        }
        return true;
    }

/*
    public boolean copy(List<Long> taskIds) {
        AssertUtils.isTrue(CollectionUtils.isNotEmpty(taskIds), "ids missing");
        taskIds.forEach(id -> {
            PaidContentActivityProgressTask task = paidContentActivityProgressTaskDelegate.selectById(id);
            AssertUtils.isTrue(task != null, "task missing");

            long oldTaskId = task.getId();
            task.setId(null);
            if (task.getStartTime() > 0) {
                long timeBetween = task.getEndTime() - task.getEndTime();
                task.setStartTime(task.getEndTime());
                task.setEndTime(task.getStartTime() + timeBetween);
            }
            paidContentActivityProgressTaskDelegate.insertValue(task);

            Map<Long, List<PaidContentActivityReward>> rewardsMap = rewardMapperDelegate.getTaskRewards(task.getActId(), Lists.newArrayList(oldTaskId));
            rewardsMap.forEach((k, v) -> {
                v.forEach(reward -> {
                    reward.setId(null);
                    reward.setTaskId(oldTaskId);
                    rewardMapperDelegate.insertValue(reward);
                });
            });
        });
        return true;
    }
*/

    public boolean delete(Long id) {
        AssertUtils.notNull(id, "id missing");
        int count = paidContentActivityProgressTaskDelegate.deleteById(id);

        SqlDelete deleteReward = new SqlDelete().whenEquals("taskId", id);
        rewardMapperDelegate.delete(deleteReward);
        return count == 1;
    }

    public boolean update(PaidContentActivityProgressTaskVO paidContentActivityProgressTaskVO) {
        AssertUtils.isTrue(paidContentActivityProgressTaskVO != null && paidContentActivityProgressTaskVO.getId() != null, "id missing");
        PaidContentActivityProgressTask paidContentActivityProgressTask = paidContentActivityProgressTaskDelegate.selectById(paidContentActivityProgressTaskVO.getId());
        AssertUtils.notNull(paidContentActivityProgressTask, "id illegal");
        BeanUtils.copyNonNullProperties(paidContentActivityProgressTaskVO, paidContentActivityProgressTask);

        if (paidContentActivityProgressTask.getTip() == null) {
            paidContentActivityProgressTask.setTip("");
        }

        paidContentActivityProgressTaskDelegate.updateValue(paidContentActivityProgressTask);


        List<Long> toRemove = rewardMapperDelegate.getRewardIds(paidContentActivityProgressTask.getActId(), Lists.newArrayList(paidContentActivityProgressTask.getId()));
        if (CollectionUtils.isNotEmpty(paidContentActivityProgressTaskVO.getRewards())) {
            paidContentActivityProgressTaskVO.getRewards().forEach(PaidContentActivityRewardVO::convertToExt);
            List<PaidContentActivityReward> rewards = new ArrayList<>();
            paidContentActivityProgressTaskVO.getRewards().forEach(item -> {
                PaidContentActivityReward reward = new PaidContentActivityReward();
                BeanUtils.copyNonNullProperties(item, reward);
                rewards.add(reward);
            });
            org.springframework.beans.BeanUtils.copyProperties(paidContentActivityProgressTaskVO.getRewards(),rewards);
            List<PaidContentActivityReward> toUpdate = rewards.stream().filter(s->(s.getId() != null && s.getId() > 0L)).collect(Collectors.toList());
            toUpdate.forEach(reward -> {
                reward.setRewardRank(0);
                reward.setRewardSchema("");
                reward.setTip("");
                SqlUpdate update  = new SqlUpdate()
                        .setFields("message", "name","img","type", "tip","rewardRank","targetId","count", "ext")
                        .whenEquals("id", reward.getId());
                rewardMapperDelegate.update(update, reward);
                toRemove.remove(reward.getId());
            });

            List<PaidContentActivityReward> toAdd = rewards.stream().filter(s->(s.getId() == null)).collect(Collectors.toList());
            toAdd.forEach(reward -> {
                reward.setActId(paidContentActivityProgressTask.getActId());
                reward.setStatus(0);
                reward.setTaskId(paidContentActivityProgressTask.getId());
                reward.setRewardRank(0);
                reward.setRewardSchema("");
                reward.setTip("");
                if(StringUtils.isEmpty(reward.getMessage())) {
                    reward.setMessage("");
                }
                rewardMapperDelegate.insertValue(reward);
            });

            if(CollectionUtils.isNotEmpty(toRemove)) {
                toRemove.forEach(id-> {
                    rewardMapperDelegate.deleteById(id);
                });
            }
        }
        return true;
    }

    private List<PaidContentActivityProgressTaskVO> populate2VOList(List<PaidContentActivityProgressTask> list) {
        return BeanConvertUtils.convertList(list, PaidContentActivityProgressTaskVO.class);
    }


}
