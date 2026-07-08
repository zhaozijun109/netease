package com.netease.lofter.tango.impl.service.clickhouse;

import com.netease.lofter.tango.impl.mapper.clickhouse.ClickHouseBlogMapper;
import com.netease.lofter.tango.impl.util.AssertUtils;
import com.netease.lofter.tango.impl.web.query.clickhouse.ClickHouseBlogQuery;
import com.netease.lofter.tango.impl.web.vo.PageResult;
import com.netease.lofter.tango.impl.web.vo.Result;
import com.netease.lofter.tango.impl.web.vo.clickhouse.ClickhouseBlogVO;
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
public class ClickHouseBlogService {
    @Autowired
    private ClickHouseBlogMapper clickHouseBlogMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private int max = 500000;

    public PageResult<ClickhouseBlogVO> listByQuery(ClickHouseBlogQuery query) {
        PageResult<ClickhouseBlogVO> pageResult = new PageResult<>(query.getPage());
        Map<String, Object> params = query.buildMap();
        Long total = clickHouseBlogMapper.countBlog(params);
        if (total <= 0) {
            return pageResult;
        }
        AssertUtils.isTrue(total <= max, "请缩小查询范围，最多查询50万条数据");
        List<ClickhouseBlogVO> clickhouseBlogVOS = clickHouseBlogMapper.listBlog(params);
        return pageResult.total(total.intValue()).list(clickhouseBlogVOS);
    }

    public void download(ClickHouseBlogQuery query, HttpServletResponse response) {
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

    public void downloadInternal(ClickHouseBlogQuery query, HttpServletResponse response) {
        query.getPage().setTo(1);
        query.getPage().setSize(max);
        PageResult<ClickhouseBlogVO> result = listByQuery(query);
        List<ClickhouseBlogVO> rows = result.getRecords();
        ExcelClient.write(response, new WriteConfigSupport<ClickhouseBlogVO>() {
            @Override
            public List<ClickhouseBlogVO> modelList() {
                return rows;
            }

            @Override
            public String[] headers() {
                return new String[]{"日期","博客ID","昵称","作者等级","新作者等级","粉丝数","主页","创作类型", "注册时间", "首次发文时间", "最近一次发文时间", "认证称号","累计作品数","优质内容数","付费作品数","图片作品数","文字作品数","视频作品数","总热度","总小蓝手","近7日涨粉数","近30日涨粉数"};
            }

            @Override
            public String[] fieldNames() {
                return new String[]{"dt","blogId","blog_nickname","level","creator_center_level","fans_std","blog_url","post_main_content_type", "registerDate", "first_publish_date", "last_publish_date","daren_names","post_count_std","premium_post_count","pay_post_count","photo_post_count_std","text_post_count_std","video_post_count_std","receive_hot_std","receive_recommend_cnt","fans_cnt_7d","fans_cnt_30d"};
            }

            @Override
            public String title() {
                return null;
            }

            @Override
            public Class<ClickhouseBlogVO> getModelType() {
                return ClickhouseBlogVO.class;
            }
        });
    }
}
