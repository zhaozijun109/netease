package com.netease.lofter.tango.impl.service.clickhouse;

import com.netease.lofter.tango.impl.mapper.clickhouse.ClickHouseIpMapper;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.web.query.clickhouse.ClickHouseIpQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.clickhouse.ClickhouseIpVO;
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
public class ClickHouseIpService {
    @Autowired
    private ClickHouseIpMapper clickHouseIpMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private int max = 500000;

    public PageResult<ClickhouseIpVO> listByQuery(ClickHouseIpQuery query) {
        PageResult<ClickhouseIpVO> pageResult = new PageResult<>(query.getPage());
        Map<String, Object> params = query.buildMap();
        Long total = clickHouseIpMapper.countIp(params);
        if (total <= 0) {
            return pageResult;
        }
        AssertUtils.isTrue(total <= max, "请缩小查询范围，最多查询50万条数据");
        List<ClickhouseIpVO> clickhouseIpVOS = clickHouseIpMapper.listIp(params);
        return pageResult.total(total.intValue()).list(clickhouseIpVOS);
    }

    public void download(ClickHouseIpQuery query, HttpServletResponse response) {
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

    public void downloadInternal(ClickHouseIpQuery query, HttpServletResponse response) {
        query.getPage().setTo(1);
        query.getPage().setSize(max);
        PageResult<ClickhouseIpVO> result = listByQuery(query);
        List<ClickhouseIpVO> rows = result.getRecords();
        ExcelClient.write(response, new WriteConfigSupport<ClickhouseIpVO>() {
            @Override
            public List<ClickhouseIpVO> modelList() {
                return rows;
            }

            @Override
            public String[] headers() {
                return new String[]{"日期","IP","标签数","总发文人数","总发文数量","总热度值", "总小蓝手","总优质内容","总免费内容","总曝光量","总浏览量", "总礼物付费金额", "总礼物付费人数", "S发文人数","A发文人数","B发文人数","C发文人数","D发文人数","D*发文人数","无等级发文人数","S发文数量","A发文数量","B发文数量","C发文数量","D发文数量","D*发文数量","无等级发文数量","图片总发文量","文字总发文量","视频总发文量","图片总发人数","文字总发人数","视频总发人数","图片总热度值","文字总热度值","视频总热度值","图片总小蓝手","文字总小蓝手","视频总小蓝手","图片总优质内容量","文字总优质内容量","视频总优质内容量"};
            }

            @Override
            public String[] fieldNames() {
                return new String[]{"dt","ip","tag_count","post_uv","post_count","hot","recommend_count","premium_post_count","free_post_count","expose_pv","real_browse_pv","pay_gift_money", "pay_gift_uv", "level_s_post_uv","level_a_post_uv","level_b_post_uv","level_c_post_uv","level_d_post_uv","level_d_star_post_uv","level_none_post_uv","level_s_post_count","level_a_post_count","level_b_post_count","level_c_post_count","level_d_post_count","level_d_star_post_count","level_none_post_count","photo_post_count","text_post_count","video_post_count","photo_post_uv","text_post_uv","video_post_uv","photo_hot","text_hot","video_hot","photo_recommend_count","text_recommend_count","video_recommend_count","photo_premium_post_count","text_premium_post_count","video_premium_post_count"};
            }

            @Override
            public String title() {
                return null;
            }

            @Override
            public Class<ClickhouseIpVO> getModelType() {
                return ClickhouseIpVO.class;
            }
        });
    }
}
