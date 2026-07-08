package com.netease.easyml.common.util;

import org.junit.Test;

/**
 * Created by linjiuning on 2020/7/14.
 */
public class DateUtilTest {

    @Test
    public void replaceDynamicDate() {
        String date = "${YYYYMMDD-5}";
        String dynamicDate = DateUtil.replaceDynamicDate(date, System.currentTimeMillis());
        System.out.println(dynamicDate);
    }
}