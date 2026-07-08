package com.netease.operator.dws.rec;

import com.netease.pojo.rec.RecActionLabel;
import com.netease.pojo.rec.RecRequest;
import com.netease.pojo.rec.RecRequestAndRecActionLabel;
import org.apache.flink.streaming.api.functions.co.ProcessJoinFunction;
import org.apache.flink.util.Collector;

public class DwsRARProcessFunction
        extends ProcessJoinFunction<RecRequest, RecActionLabel, RecRequestAndRecActionLabel> {
    @Override
    public void processElement(
            RecRequest recRequest,
            RecActionLabel recActionLabel,
            ProcessJoinFunction<RecRequest, RecActionLabel, RecRequestAndRecActionLabel>.Context
                    context,
            Collector<RecRequestAndRecActionLabel> collector)
            throws Exception {
        RecRequestAndRecActionLabel recRequestAndActionLabel = new RecRequestAndRecActionLabel();
        recRequestAndActionLabel.setItemId(recActionLabel.getItemId());
        recRequestAndActionLabel.setUserId(recActionLabel.getUserId());
        recRequestAndActionLabel.setRecId(recActionLabel.getRecId());
        recRequestAndActionLabel.setItemType(recActionLabel.getItemType());
        recRequestAndActionLabel.setRating(recActionLabel.getRating());
        recRequestAndActionLabel.setPage(recActionLabel.getPage());
        recRequestAndActionLabel.setAlg(recActionLabel.getAlg());
        recRequestAndActionLabel.setPlatform(recActionLabel.getPlatform());
        recRequestAndActionLabel.setSceneName(recActionLabel.getSceneName());
        recRequestAndActionLabel.setSourceScene(recActionLabel.getSourceScene());
        recRequestAndActionLabel.setCostTime(recActionLabel.getCostTime());
        recRequestAndActionLabel.setExpTime(recActionLabel.getExpTime());
        recRequestAndActionLabel.setOg(recActionLabel.getOg());
        recRequestAndActionLabel.setLastActionTime(recActionLabel.getLastActionTime());
        recRequestAndActionLabel.setHour(recActionLabel.getHour());
        recRequestAndActionLabel.setAv(recActionLabel.getAv());
        recRequestAndActionLabel.setExtInfo(recActionLabel.getExtInfo());
        recRequestAndActionLabel.setExposedCount(recActionLabel.getExposedCount());
        recRequestAndActionLabel.setClickCount(recActionLabel.getClickCount());
        recRequestAndActionLabel.setStayCount(recActionLabel.getStayCount());
        recRequestAndActionLabel.setLightActionCount(recActionLabel.getLightActionCount());
        recRequestAndActionLabel.setPlayCount(recActionLabel.getPlayCount());
        recRequestAndActionLabel.setImageDetailCount(recActionLabel.getImageDetailCount());
        recRequestAndActionLabel.setHighActionCount(recActionLabel.getHighActionCount());
        recRequestAndActionLabel.setNegActionCount(recActionLabel.getNegActionCount());
        recRequestAndActionLabel.setShareCount(recActionLabel.getShareCount());
        recRequestAndActionLabel.setCommentCount(recActionLabel.getCommentCount());
        recRequestAndActionLabel.setLikeCount(recActionLabel.getLikeCount());
        recRequestAndActionLabel.setRecommendCount(recActionLabel.getRecommendCount());
        recRequestAndActionLabel.setCollectCount(recActionLabel.getCollectCount());
        recRequestAndActionLabel.setRepostCount(recActionLabel.getRepostCount());
        recRequestAndActionLabel.setFollowCount(recActionLabel.getFollowCount());
        recRequestAndActionLabel.setPageClickCount(recActionLabel.getPageClickCount());
        recRequestAndActionLabel.setPayCount(recActionLabel.getPayCount());
        recRequestAndActionLabel.setPos(recActionLabel.getPos());
        recRequestAndActionLabel.setClickBlogCount(recActionLabel.getClickBlogCount());
        recRequestAndActionLabel.setClickCommentCount(recActionLabel.getClickCommentCount());
        recRequestAndActionLabel.setClickCollectionCount(recActionLabel.getClickCollectionCount());
        recRequestAndActionLabel.setLikeCommentCount(recActionLabel.getLikeCommentCount());
        recRequestAndActionLabel.setClickGiftCount(recActionLabel.getClickGiftCount());
        recRequestAndActionLabel.setClickTicketCount(recActionLabel.getClickTicketCount());
        recRequestAndActionLabel.setGiftCommentCount(recActionLabel.getGiftCommentCount());
        recRequestAndActionLabel.setFollowScCount(recActionLabel.getFollowScCount());
        recRequestAndActionLabel.setAb(recRequest.getAb());
        recRequestAndActionLabel.setReqExt(recRequest.getReqExt());
        recRequestAndActionLabel.setFlowName(recRequest.getFlowName());
        recRequestAndActionLabel.setRecReasonType(recRequest.getRecReasonType());
        recRequestAndActionLabel.setRecTime(recRequest.getRecTime());
        collector.collect(recRequestAndActionLabel);
    }
}
