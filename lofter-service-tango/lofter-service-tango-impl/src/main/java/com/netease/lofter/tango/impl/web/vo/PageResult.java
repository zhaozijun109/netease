package com.netease.lofter.tango.impl.web.vo;

import com.netease.lofter.tango.impl.entity.PageDO;
import com.netease.lofter.tango.impl.web.ro.PageRO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 7863924682017386292L;

    private PageVO page = new PageVO();

    private List<T> records = new ArrayList<>();

    public PageResult(PageRO pageRO) {
        this.page.setCurrent(pageRO.getTo());
        this.page.setSize(pageRO.getSize());
    }

    public PageResult<T> page(PageDO<T> pageDO) {
        return page(pageDO.getTotal(), pageDO.getList());
    }

    public PageResult<T> page(int total, List<T> list) {
        return total(total)
                .list(list);
    }

    public PageResult<T> total(int total) {
        //总数
        this.page.setRecordCount(total);
        //计算页数
        this.page.setTotal(calPages(total, page.getSize()));
        // 如果当前页码大于总页数，则设置为最后一页
        if (this.page.getTotal() < this.page.getCurrent()) {
            this.page.setCurrent(this.page.getTotal());
        }
        return this;
    }

    public PageResult<T> list(List<T> list) {
        this.records = list;
        return this;
    }


    private int calPages(int total, int size) {
        if (total <= 0) {
            return 1;
        }
        return (int) Math.ceil((double) total / size);
    }


}
