package com.netease.lofter.tango.impl.service.clickhouse;

import com.netease.lofter.tango.impl.mapper.clickhouse.ClickHousePostMapper;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.web.query.clickhouse.ClickHousePostQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.clickhouse.ClickhousePostExcelVO;
import com.netease.lofter.tango.impl.web.vo.clickhouse.ClickhousePostVO;
import com.netease.mm.tk.common.util.BeanConvertUtils;
import com.netease.mm.tk.common.util.lang.CollectionUtils3;
import com.netease.mm.tk.common.util.lang.ExecutionUtils;
import com.netease.mm.tk.common.util.web.WebUtils;
import com.netease.mm.tk.excel.ExcelClient;
import com.netease.mm.tk.excel.config.WriteConfigSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class ClickHousePostService {

    @Autowired
    private ClickHousePostMapper clickHousePostMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private int max = 200000;

    public PageResult<ClickhousePostVO> listByQuery(ClickHousePostQuery query) {
        PageResult<ClickhousePostVO> pageResult = new PageResult<>(query.getPage());
        Map<String, Object> params = query.buildMap();
        Long total = clickHousePostMapper.countPost(params);
        if (total <= 0) {
            return pageResult;
        }
        AssertUtils.isTrue(total <= max, "请缩小查询范围，最多查询" + max + "万条数据");
        List<ClickhousePostVO> clickhousePostVOS = clickHousePostMapper.listPost(params);
        return pageResult.total(total.intValue()).list(clickhousePostVOS);
    }

    public void download(ClickHousePostQuery query, HttpServletResponse response) {
        String key = "clickhouse_download_limit";
        Boolean succ = false;
        try {
            succ = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(10));
            if (succ != null && succ) {
                downloadInternal(query, response);
            } else {
                ExecutionUtils.executeSilently(() -> WebUtils.commitResponse(response, Result.genericFail("当前已有下载任务，请稍后重试")));
            }
        } finally {
            if (succ != null && succ) {
                stringRedisTemplate.delete(key);
            }
        }
    }

    public void downloadInternal(ClickHousePostQuery query, HttpServletResponse response) {
        query.getPage().setTo(1);
        query.getPage().setSize(max);
        PageResult<ClickhousePostVO> result = listByQuery(query);
        final List<ClickhousePostExcelVO> excelVOS = CollectionUtils3.mapper(result.getRecords(), vo -> {
            ClickhousePostExcelVO excelVO = BeanConvertUtils.convertBean(vo, ClickhousePostExcelVO.class);
            excelVO.setIfShuaReStr(vo.getIfShuaHot() ? "是" : "否");
            excelVO.setColdStartTrafficRatioStr(excelVO.formatPercent(vo.getColdStartTrafficRatio()));
            excelVO.setTagRecTrafficRatioStr(excelVO.formatPercent(vo.getTagRecTrafficRatio()));
            excelVO.setFeedRecTrafficRatioStr(excelVO.formatPercent(vo.getFeedRecTrafficRatio()));
            excelVO.setRecTrafficRatioStr(excelVO.formatPercent(vo.getRecTrafficRatio()));
            return excelVO;
        });
        result.setRecords(null);
        ExcelClient.write(response, new WriteConfigSupport<ClickhousePostExcelVO>() {
            @Override
            public List<ClickhousePostExcelVO> modelList() {
                return excelVOS;
            }

            @Override
            public String[] headers() {
                return new String[]{"文章ID", "发布时间", "文章链接", "文章所添加标签", "文章类型", "标题", "审核状态", "是否标签过滤", "是否生态优质", "用户ID", "创作者等级", "达人认证名称", "博客昵称", "blogName", "创作类型", "博客发文数", "博客粉丝数", "总热度", "总评论量", "总喜欢量", "总转载量", "总推荐量", "曝光量", "点击量", "推荐曝光占比", "冷启曝光占比", "标签页曝光占比", "发现页曝光占比", "回礼类型", "免费送礼人数", "免费礼物个数", "付费礼物人数", "付费礼物金额", "是否刷热", "刷热值"};
            }

            @Override
            public String[] fieldNames() {
                return new String[]{"postId", "publishDate", "postUrl", "tags", "contentType", "title", "recomStatus", "tagForbid", "premium", "userId", "level", "authenticateNames", "blogNickName", "blogName", "postMainContentType", "postNum", "beenFollowedUV", "hot", "commendCount", "praiseCount", "reproduceCount", "recommendCount", "exposedCount", "clickCount", "recTrafficRatioStr", "coldStartTrafficRatioStr", "tagRecTrafficRatioStr", "feedRecTrafficRatioStr", "returnGiftPlanType", "freeGiftUserCount", "freeGiftCount", "payGiftUserCount", "payGiftAmount", "ifShuaReStr", "shuaHot"};
            }


            @Override
            public String title() {
                return null;
            }

            @Override
            public Class<ClickhousePostExcelVO> getModelType() {
                return ClickhousePostExcelVO.class;
            }
        });
    }
}
