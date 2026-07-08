package com.netease.lofter.tango.impl.web.ro;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class PageRO implements Serializable {

    private static final long serialVersionUID = 333351182241055286L;
    /**
     * 必选, 原页，默认为第一页
     */
    private Integer from = 1;

    /**
     * 必选, 去向页，默认为第一页
     */
    private Integer to = 1;

    /**
     * 必选, 每页数量， 默认10条
     */
    private Integer size = 10;

    /**
     * 可选, 游标， 服务端返回，下次请求原样传入
     */
    private String cursor;
}
