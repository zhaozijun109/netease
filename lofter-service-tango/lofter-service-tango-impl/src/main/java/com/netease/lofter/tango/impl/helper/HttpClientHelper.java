package com.netease.lofter.tango.impl.helper;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.netease.lofter.tango.impl.util.AssertUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class HttpClientHelper {

    @Autowired
    private RestTemplate restTemplate;

    public JSONObject getForm(String url, Map<String, String> headers) {
        String res = getFormStr(url, headers);
        return JSONObject.parseObject(res);
    }

    public String getFormStr(String url, Map<String, String> headers) {
        return get(url, headers, null, true);
    }

    public String get(String url, Map<String, String> header, Map<String, String> params, boolean form) {
        if (null != params) {
            if (!url.contains("?")) {
                url += "?";
            }
            url += Joiner.on("&").withKeyValueSeparator("=").join(params);
        }
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        if (header != null) {
            header.remove(HttpHeaders.CONTENT_TYPE);
            header.forEach((k, v) -> {
                if (StringUtils.isNotBlank(v)) {
                    headers.add(k, v);
                }
            });
        }
        if (form) {
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        } else {
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        }
        HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        AssertUtils.isTrue(response.getStatusCode().is2xxSuccessful(), response.getStatusCode().getReasonPhrase());
        return response.getBody();
    }


    public JSONObject postForm(String url, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        if (null != params) {
            params.forEach(body::add);
        }
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        AssertUtils.isTrue(responseEntity.getStatusCode().is2xxSuccessful(), responseEntity.getStatusCode().getReasonPhrase());
        return JSONObject.parseObject(responseEntity.getBody());
    }


    public JSONObject get(String url) {
        return get(url, null);
    }

    public JSONObject get(String url, Map<String, String> headers) {
        return JSONObject.parseObject(get(url, headers, null, false));
    }

    public JSONObject post(String url, Map<String, String> params) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, request, String.class);
        AssertUtils.isTrue(responseEntity.getStatusCode().is2xxSuccessful(), responseEntity.getStatusCode().getReasonPhrase());
        return JSONObject.parseObject(responseEntity.getBody());
    }

}
