package com.netease.yuanqi.common.utils.es;

import org.apache.http.HttpHost;

public class EsUtils {

    public static HttpHost[] parseEsHosts(String esHost) {
        String[] esHosts = esHost.split(",");
        HttpHost[] esHttpHosts = new HttpHost[esHosts.length];
        for (int i = 0; i < esHosts.length; i++) {
            esHttpHosts[i] =
                    new HttpHost(
                            esHosts[i].split(":")[0],
                            Integer.parseInt(esHosts[i].split(":")[1]),
                            "http");
        }
        return esHttpHosts;
    }
}
