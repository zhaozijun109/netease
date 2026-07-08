package com.netease.lofter.tango.impl.delegate.trade.activity;

import com.netease.lofter.tango.impl.entity.PageDO;

import com.netease.yaolu.commons.spring.mybatis.sql.SqlDelete;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Primary;
import com.netease.yaolu.commons.spring.mybatis.service.CommonService;
import com.netease.yaolu.commons.spring.mybatis.service.DeleteCommonService;
import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityReward.Fields;

import com.netease.lofter.tango.impl.entity.trade.activity.PaidContentActivityReward;
import com.netease.lofter.tango.impl.mapper.trade.activity.PaidContentActivityRewardMapper;

@Primary
@Service
public class PaidContentActivityRewardMapperDelegate implements CommonService<PaidContentActivityRewardMapper, PaidContentActivityReward>, DeleteCommonService<PaidContentActivityRewardMapper, PaidContentActivityReward>{

    public int deleteByTargetId(Long targetId) {
        SqlDelete sqlSelect = new SqlDelete().whenEquals("targetId", targetId);
        return delete(sqlSelect);
    }

    public List<PaidContentActivityReward> getByTargetIds(List<Long> targetIds, int type) {
        SqlSelect sqlSelect = new SqlSelect().whenIn("targetId", targetIds).andEquals("type", type);
        return selectListNoLimit(sqlSelect);
    }

    public List<Long> getRewardIds(Long actId, List<Long> taskIds) {
        SqlSelect sqlSelect = new SqlSelect().whenIn("taskId", taskIds);
        if(actId != null && actId > 0) {
            sqlSelect.andEquals("actId", actId);
        }
        return selectListNoLimit(sqlSelect).stream().map(PaidContentActivityReward::getId).collect(Collectors.toList());
    }

    public Map<Long, List<PaidContentActivityReward>> getTaskRewards(Long actId, List<Long> taskIds) {
        SqlSelect sqlSelect = new SqlSelect().whenIn("taskId", taskIds);
        if(actId != null && actId > 0) {
            sqlSelect.andEquals("actId", actId);
        }
        return selectListNoLimit(sqlSelect).stream().collect(Collectors.groupingBy(PaidContentActivityReward::getTaskId));
    }

    public PageDO<PaidContentActivityReward> queryByActId(Long actId,int type, int offset, int limit){
        if(actId == null || actId <= 0) {
            return PageDO.empty();
        }
        SqlSelect sqlSelect = new SqlSelect().whenEquals("actId", actId).andEquals("type", type).orderDesc(Fields.id);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.limit(limit).offset(offset);
        List<PaidContentActivityReward> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }
    public PageDO<PaidContentActivityReward> listByQuery(
                                                Long createTimeBegin,
                                                Long createTimeEnd,
                                                Long id,
                                                String name,
                                                Long actId,
                                                Long taskId,
                                                String img,
                                                Integer type,
                                                Integer status,
                                                Integer count,
                                                Long targetId,
                                                String rewardSchema,
                                                String tip,
                                                String message,
                                                int offset,
                                                int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, name, actId, taskId, img, type, status, count, targetId, rewardSchema, tip, message,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.id).offset(offset).limit(limit);
        List<PaidContentActivityReward> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String name,
                            Long actId,
                            Long taskId,
                            String img,
                            Integer type,
                            Integer status,
                            Integer count,
                            Long targetId,
                            String rewardSchema,
                            String tip,
                            String message,
                            Long createTimeBegin,Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(name)) {
            sqlSelect.andEquals(Fields.name, name);
        }
        if (actId != null) {
            sqlSelect.andEquals(Fields.actId, actId);
        }
        if (taskId != null) {
            sqlSelect.andEquals(Fields.taskId, taskId);
        }
        if (StringUtils.isNotBlank(img)) {
            sqlSelect.andEquals(Fields.img, img);
        }
        if (type != null) {
            sqlSelect.andEquals(Fields.type, type);
        }
        if (status != null) {
            sqlSelect.andEquals(Fields.status, status);
        }
        if (count != null) {
            sqlSelect.andEquals(Fields.count, count);
        }
        if (targetId != null) {
            sqlSelect.andEquals(Fields.targetId, targetId);
        }
        if (StringUtils.isNotBlank(rewardSchema)) {
            sqlSelect.andEquals(Fields.rewardSchema, rewardSchema);
        }
        if (StringUtils.isNotBlank(tip)) {
            sqlSelect.andEquals(Fields.tip, tip);
        }
        if (StringUtils.isNotBlank(message)) {
            sqlSelect.andEquals(Fields.message, message);
        }
    }

    private void initTimeQuery(SqlSelect sqlSelect, Long createTimeBegin, Long createTimeEnd) {
//        if (createTimeBegin != null && createTimeEnd != null) {
//            sqlSelect.andGreatThanEqual(Fields.createTime, createTimeBegin);
//            sqlSelect.andLessThanEqual(Fields.createTime, createTimeEnd);
//        } else if (createTimeEnd != null) {
//            sqlSelect.andLessThanEqual(Fields.createTime, createTimeEnd);
//        } else if (createTimeBegin != null) {
//            sqlSelect.andGreatThanEqual(Fields.createTime, createTimeBegin);
//        }
    }
}