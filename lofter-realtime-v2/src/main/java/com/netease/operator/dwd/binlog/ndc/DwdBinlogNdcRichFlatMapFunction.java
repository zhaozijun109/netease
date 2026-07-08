package com.netease.operator.dwd.binlog.ndc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netease.pojo.BinlogRow;
import com.netease.pojo.ecology.post.Post;
import com.netease.pojo.ecology.post.PostHot;
import com.netease.pojo.ecology.question.AskQuestion;
import com.netease.pojo.pve.PveRoleGroupDialogue;
import com.netease.pojo.pve.PveUserDialogue;
import com.netease.pojo.pve.PveUserPropsLog;
import com.netease.pojo.revenue.UserOrderDetails;
import com.netease.util.DateTimeFormatterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.flink.api.common.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

import java.math.BigDecimal;

public class DwdBinlogNdcRichFlatMapFunction extends RichFlatMapFunction<BinlogRow, String> {
    private static final Logger LOG =
            LoggerFactory.getLogger(DwdBinlogNdcRichFlatMapFunction.class);
    private ObjectMapper objectMapper;

    @Override
    public void open(Configuration parameters) throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public void flatMap(BinlogRow binlogRow, Collector<String> collector) throws Exception {
        if (binlogRow.getOp() == 0) { // INSERT
            if ("Ask_Question".equals(binlogRow.getTableName())) {
                collector.collect(objectMapper.writeValueAsString(setAskQuestion(binlogRow)));
            }

            if ("Trade_GiftPresentRecord".equals(binlogRow.getTableName())
                    || "Trade_StoreVipOrder".equals(binlogRow.getTableName())
                    || "Trade_FansVipOrder".equals(binlogRow.getTableName())
                    || "Trade_PVEStaminaOrder".equals(binlogRow.getTableName())) {
                collector.collect(objectMapper.writeValueAsString(setUserOrderDetails(binlogRow)));
            }

            if ("Post".equals(binlogRow.getTableName())) {
                collector.collect(objectMapper.writeValueAsString(setPost(binlogRow)));
            }

            if ("PostHot".equals(binlogRow.getTableName())) {
                collector.collect(objectMapper.writeValueAsString(setPostHot(binlogRow)));
            }

            // Todo: Replace PVE_UserDialogue with PVE_UserDialoguePartition
            if ("PVE_UserDialoguePartition".equals(binlogRow.getTableName())) {
                collector.collect(objectMapper.writeValueAsString(setPveUserDialogue(binlogRow)));
            }

            if ("PVE_UserPropsLog".equals(binlogRow.getTableName())) {
                collector.collect(objectMapper.writeValueAsString(setPveUserPropsLog(binlogRow)));
            }
        }

        if (binlogRow.getOp() == 1) { // DELETE
            if ("PostHot".equals(binlogRow.getTableName())) {
                collector.collect(objectMapper.writeValueAsString(setPostHot(binlogRow)));
            }
        }

        if (binlogRow.getOp() == 2) { // UPDATE
            if ("Ask_Question".equals(binlogRow.getTableName())) {
                collector.collect(objectMapper.writeValueAsString(setAskQuestion(binlogRow)));
            }

            if ("Post".equals(binlogRow.getTableName())) {
                collector.collect(objectMapper.writeValueAsString(setPost(binlogRow)));
            }

            if ("PVE_RoleGroupDialogue".equals(binlogRow.getTableName())) {
                Tuple2<Integer, PveRoleGroupDialogue> tuple2 = setPveRoleGroupDialogue(binlogRow);
                if (tuple2.f0 == 0) {
                    collector.collect(objectMapper.writeValueAsString(tuple2.f1));
                }
            }
        }
    }

    private PveUserPropsLog setPveUserPropsLog(BinlogRow binlogRow) throws JsonProcessingException {
        PveUserPropsLog pveUserPropsLog =
                objectMapper.readValue(
                        objectMapper.writeValueAsString(binlogRow.getAfter()),
                        PveUserPropsLog.class);
        pveUserPropsLog.setTableName(binlogRow.getTableName());

        return pveUserPropsLog;
    }

    private Tuple2<Integer, PveRoleGroupDialogue> setPveRoleGroupDialogue(BinlogRow binlogRow)
            throws JsonProcessingException {
        // Todo: Since the targetRoleIds column is inserted first and then updated in the backend
        // Todo: logic, the update instead of insert binlog is subscribed.
        PveRoleGroupDialogue beforePveRoleGroupDialogue =
                objectMapper.readValue(
                        objectMapper.writeValueAsString(binlogRow.getBefore()),
                        PveRoleGroupDialogue.class);
        PveRoleGroupDialogue afterPveRoleGroupDialogue =
                objectMapper.readValue(
                        objectMapper.writeValueAsString(binlogRow.getAfter()),
                        PveRoleGroupDialogue.class);
        if ("[]".equals(beforePveRoleGroupDialogue.getTargetRoleIds())
                && !"[]".equals(afterPveRoleGroupDialogue.getTargetRoleIds())) {
            afterPveRoleGroupDialogue.setTableName(binlogRow.getTableName());
            return Tuple2.of(0, afterPveRoleGroupDialogue);
        }
        return Tuple2.of(1, afterPveRoleGroupDialogue);
    }

    private PveUserDialogue setPveUserDialogue(BinlogRow binlogRow) throws JsonProcessingException {
        PveUserDialogue pveUserDialogue =
                objectMapper.readValue(
                        objectMapper.writeValueAsString(binlogRow.getAfter()),
                        PveUserDialogue.class);
        // Todo: Replace PVE_UserDialogue with PVE_UserDialoguePartition
        pveUserDialogue.setTableName("PVE_UserDialogue");

        return pveUserDialogue;
    }

    private PostHot setPostHot(BinlogRow binlogRow) throws JsonProcessingException {
        PostHot postHot = new PostHot();
        if (binlogRow.getOp() == 0) {
            postHot =
                    objectMapper.readValue(
                            objectMapper.writeValueAsString(binlogRow.getAfter()), PostHot.class);
            postHot.setStatus(1);
        } else if (binlogRow.getOp() == 1) {
            postHot =
                    objectMapper.readValue(
                            objectMapper.writeValueAsString(binlogRow.getBefore()), PostHot.class);
            postHot.setStatus(-1);
        }
        postHot.setTableName(binlogRow.getTableName());

        return postHot;
    }

    private Post setPost(BinlogRow binlogRow) throws JsonProcessingException {
        Post post =
                objectMapper.readValue(
                        objectMapper.writeValueAsString(binlogRow.getAfter()), Post.class);
        post.setTableName(binlogRow.getTableName());
        return post;
    }

    private AskQuestion setAskQuestion(BinlogRow binlogRow) throws JsonProcessingException {
        AskQuestion askQuestion =
                objectMapper.readValue(
                        objectMapper.writeValueAsString(binlogRow.getAfter()), AskQuestion.class);

        if (binlogRow.getOp() == 0) {
            askQuestion.setDeltaDiscussCount(askQuestion.getDiscussCount());
            askQuestion.setDeltaScoreCount(askQuestion.getScoreCount());
        } else if (binlogRow.getOp() == 2) {
            AskQuestion beforeAskQuestion =
                    objectMapper.readValue(
                            objectMapper.writeValueAsString(binlogRow.getBefore()),
                            AskQuestion.class);
            askQuestion.setDeltaDiscussCount(
                    askQuestion.getDiscussCount() - beforeAskQuestion.getDiscussCount());
            askQuestion.setDeltaScoreCount(
                    askQuestion.getScoreCount() - beforeAskQuestion.getScoreCount());
        }
        askQuestion.setTableName(binlogRow.getTableName());
        return askQuestion;
    }

    private UserOrderDetails setUserOrderDetails(BinlogRow binlogRow) {
        DateTimeFormatterUtils dateTimeFormatterUtils = new DateTimeFormatterUtils();
        UserOrderDetails userOrderDetails = new UserOrderDetails();

        if ("Trade_GiftPresentRecord".equals(binlogRow.getTableName())) {
            userOrderDetails.setTableName(binlogRow.getTableName());
            userOrderDetails.setOrderId((Long) binlogRow.getAfter().get("id"));
            userOrderDetails.setOrderType("礼物付费");
            userOrderDetails.setUserId((Long) binlogRow.getAfter().get("sender"));
            userOrderDetails.setProductId((Long) binlogRow.getAfter().get("postid"));
            userOrderDetails.setProductNum((Integer) binlogRow.getAfter().get("count"));
            userOrderDetails.setOrderTime((Long) binlogRow.getAfter().get("createtime"));
            userOrderDetails.setPayTime((Long) binlogRow.getAfter().get("createtime"));
            userOrderDetails.setPayOrderDate(
                    dateTimeFormatterUtils.dateTimeFormat(
                            (Long) binlogRow.getAfter().get("createtime")));
            userOrderDetails.setOrderAmount(
                    BigDecimal.valueOf((Long) binlogRow.getAfter().get("coin")));
            userOrderDetails.setGiftId((Long) binlogRow.getAfter().get("giftid"));
            userOrderDetails.setPostId((Long) binlogRow.getAfter().get("postid"));
            userOrderDetails.setBlogId((Long) binlogRow.getAfter().get("blogid"));
            userOrderDetails.setStatus(
                    statusNormalize("礼物付费", (Integer) binlogRow.getAfter().get("status")));
        }

        if ("Trade_StoreVipOrder".equals(binlogRow.getTableName())) {
            userOrderDetails.setTableName(binlogRow.getTableName());
            userOrderDetails.setOrderId((Long) binlogRow.getAfter().get("id"));
            userOrderDetails.setOrderType("书城会员");
            userOrderDetails.setUserId((Long) binlogRow.getAfter().get("userid"));
            userOrderDetails.setProductId((Long) binlogRow.getAfter().get("productid"));
            userOrderDetails.setProductNum((Integer) binlogRow.getAfter().get("vipdays"));
            userOrderDetails.setOrderTime((Long) binlogRow.getAfter().get("createtime"));
            userOrderDetails.setPayTime((Long) binlogRow.getAfter().get("finishtime"));
            userOrderDetails.setPayOrderDate(
                    dateTimeFormatterUtils.dateTimeFormat(
                            (Long) binlogRow.getAfter().get("finishtime")));
            userOrderDetails.setOrderAmount((BigDecimal) binlogRow.getAfter().get("amount"));
            userOrderDetails.setGiftId(0L);
            userOrderDetails.setPostId((Long) binlogRow.getAfter().get("postid"));
            userOrderDetails.setBlogId(0L);
            userOrderDetails.setStatus(
                    statusNormalize("书城会员", (Integer) binlogRow.getAfter().get("status")));
        }

        if ("Trade_FansVipOrder".equals(binlogRow.getTableName())) {
            userOrderDetails.setTableName(binlogRow.getTableName());
            userOrderDetails.setOrderId((Long) binlogRow.getAfter().get("id"));
            userOrderDetails.setOrderType("粉丝会员");
            userOrderDetails.setUserId((Long) binlogRow.getAfter().get("userid"));
            userOrderDetails.setProductId((Long) binlogRow.getAfter().get("vipblogid"));
            userOrderDetails.setProductNum((Integer) binlogRow.getAfter().get("vipdays"));
            userOrderDetails.setOrderTime((Long) binlogRow.getAfter().get("createtime"));
            userOrderDetails.setPayTime((Long) binlogRow.getAfter().get("finishtime"));
            userOrderDetails.setPayOrderDate(
                    dateTimeFormatterUtils.dateTimeFormat(
                            (Long) binlogRow.getAfter().get("finishtime")));
            userOrderDetails.setOrderAmount((BigDecimal) binlogRow.getAfter().get("amount"));
            userOrderDetails.setGiftId(0L);
            userOrderDetails.setPostId((Long) binlogRow.getAfter().get("postid"));
            userOrderDetails.setBlogId((Long) binlogRow.getAfter().get("vipblogid"));
            userOrderDetails.setStatus(
                    statusNormalize("粉丝会员", (Integer) binlogRow.getAfter().get("status")));
        }

        if ("Trade_PVEStaminaOrder".equals(binlogRow.getTableName())) {
            userOrderDetails.setTableName(binlogRow.getTableName());
            userOrderDetails.setOrderId((Long) binlogRow.getAfter().get("tradeid"));
            userOrderDetails.setOrderType("虚拟恋人");
            userOrderDetails.setUserId((Long) binlogRow.getAfter().get("userid"));
            userOrderDetails.setProductId((Long) binlogRow.getAfter().get("productid"));
            userOrderDetails.setProductNum(0);
            userOrderDetails.setOrderTime((Long) binlogRow.getAfter().get("createtime"));
            userOrderDetails.setPayTime((Long) binlogRow.getAfter().get("finishtime"));
            userOrderDetails.setPayOrderDate(
                    dateTimeFormatterUtils.dateTimeFormat(
                            (Long) binlogRow.getAfter().get("finishtime")));
            userOrderDetails.setOrderAmount((BigDecimal) binlogRow.getAfter().get("amount"));
            userOrderDetails.setGiftId(0L);
            userOrderDetails.setPostId(0L);
            userOrderDetails.setBlogId(0L);
            userOrderDetails.setStatus(
                    statusNormalize("虚拟恋人", (Integer) binlogRow.getAfter().get("status")));
        }

        return userOrderDetails;
    }

    private Integer statusNormalize(String businessType, Integer status) {
        switch (businessType) {
            case "礼物付费":
                return status == 0 ? 1 : status;
            case "书城会员":
                return status == 0 ? 2 : status;
            case "博客订阅":
            case "粉丝会员":
                return status == 1 ? 1 : 2;
            case "直播":
                return 1;
            case "打赏":
                return status == 10 ? 1 : 2;
            default:
                return status;
        }
    }
}
