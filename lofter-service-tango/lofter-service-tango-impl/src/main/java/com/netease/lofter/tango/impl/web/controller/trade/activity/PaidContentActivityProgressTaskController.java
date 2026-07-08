package com.netease.lofter.tango.impl.web.controller.trade.activity;

import com.netease.lofter.acl.sdk.annotation.ACLResource;
import com.netease.lofter.tango.impl.entity.trade.activity.ext.VoteRewardExt;
import com.netease.lofter.tango.impl.service.trade.activity.PaidContentActivityProgressTaskService;
import com.netease.lofter.tango.impl.web.query.trade.activity.PaidContentActivityProgressTaskQuery;
import com.netease.lofter.tango.impl.web.vo.trade.activity.PaidContentActivityProgressTaskVO;

import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.PrimiaryKey;

import com.netease.lofter.tango.impl.web.vo.trade.activity.PaidContentActivityRewardVO;
import com.netease.yaolu.commons.utils.exception.BizCode;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/tango/paidContentActivityProgressTask")
@ACLResource(roles = "ACTIVITY")
public class PaidContentActivityProgressTaskController {

    @Autowired
    private PaidContentActivityProgressTaskService paidContentActivityProgressTaskService;

    @PostMapping("/list")
    public Result<PageResult<PaidContentActivityProgressTaskVO>> listByQuery(@RequestBody @Validated PaidContentActivityProgressTaskQuery paidContentActivityProgressTaskQuery) {
        return Result.success(paidContentActivityProgressTaskService.listByQuery(paidContentActivityProgressTaskQuery));
    }

    @PostMapping("/add")
    public Result<Boolean> add(@RequestBody @Validated PaidContentActivityProgressTaskVO paidContentActivityProgressTaskVO) {
        String msg = check(paidContentActivityProgressTaskVO);
        if (StringUtils.isNotBlank(msg)) {
            return Result.error(BizCode.SERVER_ERROR,msg);
        }
        return Result.success(paidContentActivityProgressTaskService.add(paidContentActivityProgressTaskVO));
    }

    @PostMapping("/update")
    public Result<Boolean> update(@RequestBody @Validated PaidContentActivityProgressTaskVO paidContentActivityProgressTaskVO) {
        String msg = check(paidContentActivityProgressTaskVO);
        if (StringUtils.isNotBlank(msg)) {
            return Result.error(BizCode.SERVER_ERROR,msg);
        }
        return Result.success(paidContentActivityProgressTaskService.update(paidContentActivityProgressTaskVO));
    }

    @PostMapping("/delete")
    public Result<Boolean> delete(@RequestBody @Validated PrimiaryKey primiaryKey) {
        return Result.success(paidContentActivityProgressTaskService.delete(primiaryKey.getId()));
    }

//    @PostMapping("/copy")
//    public Result<Boolean> copyActTask(@RequestBody @Validated CopyActTaskReq copyActTaskReq) {
//        return Result.success(paidContentActivityProgressTaskService.copy(copyActTaskReq.taskIds));
//    }

//    @Data
//    public static class CopyActTaskReq implements Serializable {
//        private static final long serialVersionUID = -1;
//        private List<Long> taskIds;
//    }

    private String check(PaidContentActivityProgressTaskVO paidContentActivityProgressTaskVO) {
        if (paidContentActivityProgressTaskVO.getRewards() == null) {
            return "奖品配置错误";
        }

        for (PaidContentActivityRewardVO item : paidContentActivityProgressTaskVO.getRewards()) {
            if (item.getType() == 15 || item.getType() == 16) {
                VoteRewardExt ext = item.convertToVoteRewardExt();
                if (ext == null) {
                    return "投票机会配置错误";
                }
            }
        }

        return null;
    }
}






