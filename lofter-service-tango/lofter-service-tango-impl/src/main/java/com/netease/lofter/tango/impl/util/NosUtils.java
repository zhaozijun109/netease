package com.netease.lofter.tango.impl.util;

import com.netease.yaolu.lofter.core.util.nos.ImageNosUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class NosUtils {

    public static String transferInner(String imageUrl) {
        try {
            if (StringUtils.isBlank(imageUrl)) {
                return imageUrl;
            }
            String bucket[] = ImageNosUtil.parseUrl(imageUrl);
            if (Objects.isNull(bucket)) {
                return imageUrl;
            }
            return String.format("http://nos2-i.service.163.org/%s/%s", bucket[0], bucket[1]);
        } catch (Exception e) {
            return imageUrl;
        }
    }
}
