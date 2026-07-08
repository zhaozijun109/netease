package com.netease.lofter.tango.impl.util;

import com.netease.lofter.tango.impl.consts.Codes;
import com.netease.yaolu.commons.utils.exception.BizCode;
import com.netease.yaolu.commons.utils.exception.ErrorCodeException;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class AssertUtils {

    private AssertUtils() {
    }

    public static void notNull(Object object, String msg) {
        isTrue(Objects.nonNull(object), msg);
    }

    public static void notBlank(String content, String msg) {
        isTrue(StringUtils.isNotBlank(content), msg);
    }


    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw bizException(message);
        }
    }

    public static ErrorCodeException bizException(String msg) {
        return new ErrorCodeException(new BizCode(Codes.Common.ERR_BAD_REQUEST.getCode(), msg));
    }
}

