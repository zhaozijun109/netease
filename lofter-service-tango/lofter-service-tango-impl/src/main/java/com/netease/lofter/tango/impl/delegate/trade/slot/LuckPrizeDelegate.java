
package com.netease.lofter.tango.impl.delegate.trade.slot;


import com.netease.lofter.tango.impl.delegate.common.CommonDeleteDelegate;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrize;
import com.netease.lofter.tango.impl.entity.trade.slot.LuckPrize.Fields;
import com.netease.lofter.tango.impl.mapper.trade.slot.LuckPrizeMapper;
import com.netease.lofter.tango.impl.entity.PageDO;

import com.netease.yaolu.commons.spring.mybatis.sql.SqlSelect;
import org.springframework.stereotype.Service;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

 /**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2019</p>
 * <p>@author: shiliang</p>
 * <p>@Create Time: 2024-7-22 21:05:49</p>
 */
@Service
public class LuckPrizeDelegate implements CommonDeleteDelegate<LuckPrizeMapper, LuckPrize> {

    public PageDO<LuckPrize> listByQuery(Long createTimeBegin, Long createTimeEnd,
        Long id, String appKey, String activityId, String name, String picUrl, Integer prizeType, String thirdpartLink, Integer count, Integer showFlag, Integer showCount, Integer indexId, Integer price, Integer dailyBingoLimit, Integer userBingoLimit, Byte bingoCheckFlag, String bingoNoticeContent, Integer bingoCount, String customInfo,  int offset, int limit) {
        SqlSelect sqlSelect = new SqlSelect();
        this.buildQuery(sqlSelect, id, appKey, activityId, name, picUrl, prizeType, thirdpartLink, count, showFlag, showCount, indexId, price, dailyBingoLimit, userBingoLimit, bingoCheckFlag, bingoNoticeContent, bingoCount, customInfo,  createTimeBegin, createTimeEnd);
        int _count = count(sqlSelect);
        if (_count == 0) {
            return PageDO.empty();
        }
        sqlSelect.orderDesc(Fields.createTime).offset(offset).limit(limit);
        List<LuckPrize> list = selectList(sqlSelect);
        return new PageDO<>(_count, list);
    }

    private void buildQuery(SqlSelect sqlSelect,
                            Long id,
                            String appKey,
                            String activityId,
                            String name,
                            String picUrl,
                            Integer prizeType,
                            String thirdpartLink,
                            Integer count,
                            Integer showFlag,
                            Integer showCount,
                            Integer indexId,
                            Integer price,
                            Integer dailyBingoLimit,
                            Integer userBingoLimit,
                            Byte bingoCheckFlag,
                            String bingoNoticeContent,
                            Integer bingoCount,
                            String customInfo,
                            Long createTimeBegin, Long createTimeEnd) {
        this.initTimeQuery(sqlSelect, createTimeBegin, createTimeEnd);
        if (id != null) {
            sqlSelect.andEquals(Fields.id, id);
        }
        if (StringUtils.isNotBlank(appKey)) {
            sqlSelect.andEquals(Fields.appKey, appKey);
        }
        if (StringUtils.isNotBlank(activityId)) {
            sqlSelect.andEquals(Fields.activityId, activityId);
        }
        if (StringUtils.isNotBlank(name)) {
            sqlSelect.andEquals(Fields.name, name);
        }
        if (StringUtils.isNotBlank(picUrl)) {
            sqlSelect.andEquals(Fields.picUrl, picUrl);
        }
        if (prizeType != null) {
            sqlSelect.andEquals(Fields.prizeType, prizeType);
        }
        if (StringUtils.isNotBlank(thirdpartLink)) {
            sqlSelect.andEquals(Fields.thirdpartLink, thirdpartLink);
        }
        if (count != null) {
            sqlSelect.andEquals(Fields.count, count);
        }
        if (showFlag != null) {
            sqlSelect.andEquals(Fields.showFlag, showFlag);
        }
        if (showCount != null) {
            sqlSelect.andEquals(Fields.showCount, showCount);
        }
        if (indexId != null) {
            sqlSelect.andEquals(Fields.indexId, indexId);
        }
        if (price != null) {
            sqlSelect.andEquals(Fields.price, price);
        }
        if (dailyBingoLimit != null) {
            sqlSelect.andEquals(Fields.dailyBingoLimit, dailyBingoLimit);
        }
        if (userBingoLimit != null) {
            sqlSelect.andEquals(Fields.userBingoLimit, userBingoLimit);
        }
        if (bingoCheckFlag != null) {
            sqlSelect.andEquals(Fields.bingoCheckFlag, bingoCheckFlag);
        }
        if (StringUtils.isNotBlank(bingoNoticeContent)) {
            sqlSelect.andEquals(Fields.bingoNoticeContent, bingoNoticeContent);
        }
        if (bingoCount != null) {
            sqlSelect.andEquals(Fields.bingoCount, bingoCount);
        }
        if (StringUtils.isNotBlank(customInfo)) {
            sqlSelect.andEquals(Fields.customInfo, customInfo);
        }
    }

    private void initTimeQuery(SqlSelect sqlSelect, Long createTimeBegin, Long createTimeEnd) {
        if (createTimeBegin != null && createTimeEnd != null) {
            sqlSelect.andGreatThanEqual(Fields.createTime, createTimeBegin);
            sqlSelect.andLessThanEqual(Fields.createTime, createTimeEnd);
        } else if (createTimeEnd != null) {
            sqlSelect.andLessThanEqual(Fields.createTime, createTimeEnd);
        } else if (createTimeBegin != null) {
            sqlSelect.andGreatThanEqual(Fields.createTime, createTimeBegin);
        }
    }

}







