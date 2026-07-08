package com.netease.lofter.tango.impl.web.ro;

import com.google.common.base.Joiner;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.TreeMap;

@Getter
@Setter
public class ConfigGetRO implements Serializable {
    private static final long serialVersionUID = -2686592792519610771L;

    @NotBlank(message = "appId missing")
    private String appId;

    @NotNull(message = "keys missing")
    @Size(min = 1, max = 20, message = "key数量限制1-20")
    private List<String> keys;

    @Length(min = 16, max = 16, message = "nonce length illegal")
    @NotBlank(message = "nonce missing")
    private String nonce;

    @NotNull(message = "timestamp missing")
    private Long timestamp;

    @NotBlank(message = "sign missing")
    private String sign;

    private String referer;


    public boolean verifySign(String referer) {
        if (!isValid(referer)) {
            return false;
        }
        Duration duration = Duration.ofMillis(System.currentTimeMillis() - timestamp);
        if (Math.abs(duration.toMinutes()) > 5) {
            return false;
        }
        TreeMap<String, String> params = new TreeMap<>(String::compareTo);
        params.put("appId", appId);
        params.put("keys", String.join(",", keys));
        params.put("nonce", nonce);
        params.put("timestamp", timestamp.toString());
        params.put("referer", referer);
        String plainText = Joiner.on("&").withKeyValueSeparator("=").join(params);
        String sign = DigestUtils.md5Hex(plainText);
        return sign.equals(this.sign);
    }


    private boolean isValid(String referer) {
        if (StringUtils.isBlank(referer)) {
            return false;
        }
        try {
            URL url = new URL(referer);
            String host = url.getHost();
            if (host.endsWith(".hz.netease.com")) {
                return true;
            }
            if (host.endsWith(".lofter.com")) {
                return true;
            }
        } catch (MalformedURLException e) {
            // ignore;
        }
        return false;
    }
}
