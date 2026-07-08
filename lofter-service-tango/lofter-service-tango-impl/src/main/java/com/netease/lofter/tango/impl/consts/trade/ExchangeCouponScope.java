package com.netease.lofter.tango.impl.consts.trade;

import com.netease.yaolu.commons.core.enumeration.StringEnum;

public enum ExchangeCouponScope implements StringEnum {
    GIFT("GIFT", "礼物"),


    ;

    private String value;

    private String desc;

    ExchangeCouponScope(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }


    @Override
    public String getStringValue() {
        return value;
    }

    @Override
    public boolean isNull() {
        return false;
    }
}
