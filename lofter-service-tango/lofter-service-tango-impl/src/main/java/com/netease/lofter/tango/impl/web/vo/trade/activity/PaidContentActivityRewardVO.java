package com.netease.lofter.tango.impl.web.vo.trade.activity;

import java.io.Serializable;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.netease.lofter.tango.impl.entity.trade.activity.ext.VoteRewardExt;
import com.netease.yaolu.commons.core.NumberUtil;
import com.netease.yaolu.lofter.core.util.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;


/**
 * 内容付费-奖品
 * generate by yaolu mybatis generator
 */
@Getter
@Setter
public class PaidContentActivityRewardVO implements Serializable {
    private static final long serialVersionUID = -1;

    /**
     * 主键id
     */
    private Long id;
    /**
     * 名称
     */
    private String name;
    /**
     * 活动id
     */
    private Long actId;
    /**
     * 活动任务id
     */
    private Long taskId;
    /**
     * 配图
     */
    private String img;
    /**
     * 奖品类型
     */
    private Integer type;
    /**
     * 状态 0:正常; -1:停发（处理一些突发情况）
     */
    private Integer status;
    /**
     * 奖品发放数量
     */
    private Integer count;
    /**
     * 活动id
     */
    private Long targetId;
    /**
     * 奖励跳转链接
     */
    private String rewardSchema;
    /**
     * 奖励描述文案
     */
    private String tip;
    /**
     * 奖励私信文案模板
     */
    private String message;

    private String ext;
    ;
    private String voteActivityId;
    private String rankIds;

    public VoteRewardExt convertToVoteRewardExt() {
        // type 15 投票机会
        if (!NumberUtil.in(type,15,16 ) || StringUtils.isEmpty(voteActivityId) || StringUtils.isEmpty(rankIds)) {
            return null;
        }
        VoteRewardExt voteRewardExt = new VoteRewardExt();
        voteRewardExt.setActivityId(voteActivityId);
        voteRewardExt.setRankIds(Lists.newArrayList(rankIds.split(","))
                .stream()
                .filter(StringUtils::isNotEmpty)
                .filter(StringUtils::isNumeric)
                .map(Long::parseLong).collect(Collectors.toList()));

        if (CollectionUtils.isEmpty(voteRewardExt.getRankIds())) {
            return null;
        }
        return voteRewardExt;
    }

    public void convertToExt() {
        VoteRewardExt voteRewardExt = convertToVoteRewardExt();
        if (voteRewardExt == null) {
            return;
        }
        ext = JsonUtils.toJsonString(voteRewardExt);
    }

    public void conventStr() {
        if (!NumberUtil.in(type,15,16 ) || StringUtils.isEmpty(ext)) {
            return;
        }

        VoteRewardExt voteRewardExt = JsonUtils.toObject(ext, VoteRewardExt.class);
        voteActivityId = voteRewardExt.getActivityId();
        rankIds = StringUtils.join(voteRewardExt.getRankIds(), ",");
    }
}