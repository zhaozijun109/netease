package com.netease.lofter.tango.api.consts;

public enum TangoConfigOpType {
    ADD("新增"),
    UPDATE("更新"),
    DELETE("删除"),
    ROLLBACK("回滚"),


    ;
    public final String DESC;

    TangoConfigOpType(String desc) {
        this.DESC = desc;
    }
}
