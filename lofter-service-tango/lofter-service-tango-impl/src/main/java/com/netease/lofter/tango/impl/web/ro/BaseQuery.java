package com.netease.lofter.tango.impl.web.ro;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 带有分页信息的查询条件
 */
@Getter
@Setter
public class BaseQuery implements Serializable {

    private static final long serialVersionUID = -4062430620137648913L;

    private PageRO page = new PageRO();

    public int getOffset() {
        return (page.getTo() - 1) * page.getSize();
    }

    public int getLimit() {
        return this.page.getSize();
    }

    public int getPageNum() {
        return this.page.getTo();
    }

    public int getPageSize() {
        return this.page.getSize();
    }

    public String getCurosr() {
        return this.page.getCursor();
    }

}
