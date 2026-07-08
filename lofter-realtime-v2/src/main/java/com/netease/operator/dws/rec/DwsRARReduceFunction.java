package com.netease.operator.dws.rec;

import com.netease.pojo.rec.RecRequestAndRecActionLabel;
import org.apache.flink.api.common.functions.ReduceFunction;

public class DwsRARReduceFunction implements ReduceFunction<RecRequestAndRecActionLabel> {
    @Override
    public RecRequestAndRecActionLabel reduce(
            RecRequestAndRecActionLabel t1, RecRequestAndRecActionLabel t2) throws Exception {
        t1.setAlg(t1.getAlg().compareTo(t2.getAlg()) > 0 ? t1.getAlg() : t2.getAlg());
        t1.setSourceScene(
                t1.getSourceScene().compareTo(t2.getSourceScene()) > 0
                        ? t1.getSourceScene()
                        : t2.getSourceScene());
        t1.setCostTime(t1.getCostTime() + t2.getCostTime());
        t1.setExpTime(t1.getExpTime() + t2.getExpTime());
        t1.setOg(
                t1.getOg() != null && t2.getOg() != null
                        ? t1.getOg() > t2.getOg() ? t1.getOg() : t2.getOg()
                        : null);
        t1.setLastActionTime(
                t1.getLastActionTime() > t2.getLastActionTime()
                        ? t1.getLastActionTime()
                        : t2.getLastActionTime());
        t1.setHour(t1.getHour().compareTo(t2.getHour()) > 0 ? t1.getHour() : t2.getHour());
        t1.setExtInfo(
                t1.getExtInfo() != null && t2.getExtInfo() != null
                        ? t1.getExtInfo().compareTo(t2.getExtInfo()) > 0
                                ? t1.getExtInfo()
                                : t2.getExtInfo()
                        : null);
        t1.setExposedCount(t1.getExposedCount() + t2.getExposedCount());
        t1.setClickCount(t1.getClickCount() + t2.getClickCount());
        t1.setStayCount(t1.getStayCount() + t2.getStayCount());
        t1.setLightActionCount(t1.getLightActionCount() + t2.getLightActionCount());
        t1.setPlayCount(t1.getPlayCount() + t2.getPlayCount());
        t1.setImageDetailCount(t1.getImageDetailCount() + t2.getImageDetailCount());
        t1.setHighActionCount(t1.getHighActionCount() + t2.getHighActionCount());
        t1.setNegActionCount(t1.getNegActionCount() + t2.getNegActionCount());
        t1.setShareCount(t1.getShareCount() + t2.getShareCount());
        t1.setCommentCount(t1.getCommentCount() + t2.getCommentCount());
        t1.setLikeCount(t1.getLikeCount() + t2.getLikeCount());
        t1.setRecommendCount(t1.getRecommendCount() + t2.getRecommendCount());
        t1.setCollectCount(t1.getCollectCount() + t2.getCollectCount());
        t1.setRepostCount(t1.getRepostCount() + t2.getRepostCount());
        t1.setFollowCount(t1.getFollowCount() + t2.getFollowCount());
        t1.setPageClickCount(t1.getPageClickCount() + t2.getPageClickCount());
        t1.setPayCount(t1.getPayCount() + t2.getPayCount());
        t1.setPos(
                t1.getPos() != null && t2.getPos() != null
                        ? Math.min(t1.getPos(), t2.getPos())
                        : null);
        t1.setClickBlogCount(t1.getClickBlogCount() + t2.getClickBlogCount());
        t1.setClickCommentCount(t1.getClickCommentCount() + t2.getClickCommentCount());
        t1.setClickCollectionCount(t1.getClickCollectionCount() + t2.getClickCollectionCount());
        t1.setLikeCommentCount(t1.getLikeCommentCount() + t2.getLikeCommentCount());
        t1.setClickGiftCount(t1.getClickGiftCount() + t2.getClickGiftCount());
        t1.setClickTicketCount(t1.getClickTicketCount() + t2.getClickTicketCount());
        t1.setGiftCommentCount(t1.getGiftCommentCount() + t2.getGiftCommentCount());
        t1.setFollowScCount(t1.getFollowScCount() + t2.getFollowScCount());

        return t1;
    }
}
