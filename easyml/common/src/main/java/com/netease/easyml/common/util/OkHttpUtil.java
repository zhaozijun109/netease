package com.netease.easyml.common.util;

import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by linjiuning on 2020/6/22.
 */
public class OkHttpUtil {
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType BYTE = MediaType.parse("application/octet-stream");

    private volatile static OkHttpUtil mInstance;

    private OkHttpClient client;

    public OkHttpUtil() {
        this.client = new OkHttpClient();
    }

    public OkHttpUtil(OkHttpClient client) {
        if (client == null)
            this.client = new OkHttpClient.Builder()
                    .readTimeout(1, TimeUnit.MINUTES)
                    .build();
        else
            this.client = client;
    }

    // get
    public String get(String url) throws IOException {
        String res = null;
        ResponseBody rBody = getBody(url);
        if (rBody != null) {
            res = rBody.string();
            rBody.close();
        }
        return res;
    }

    public byte[] getBytes(String url) throws IOException {
        byte[] res = null;
        ResponseBody rBody = getBody(url);
        if (rBody != null) {
            res = rBody.bytes();
            rBody.close();
        }
        return res;
    }

    public ResponseBody getBody(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody rBody = null;
        if (response.isSuccessful()) {
            rBody = response.body();
        } else {
            throw new IOException("Unexpected code " + response);
        }
        return rBody;
    }

    // post json
    public String post(String url, String json) throws IOException {
        String res = null;
        ResponseBody rBody = postBody(url, json);
        if (rBody != null) {
            res = rBody.string();
            rBody.close();
        }
        return res;
    }

    public byte[] postBytes(String url, String json) throws IOException {
        byte[] res = null;
        ResponseBody rBody = postBody(url, json);
        if (rBody != null) {
            res = rBody.bytes();
            rBody.close();
        }
        return res;
    }

    public ResponseBody postBody(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        ResponseBody res = null;
        if (response.isSuccessful()) {
            res = response.body();
        } else {
            throw new IOException("Unexpected code " + response);
        }
        return res;
    }

    // post form
    public String post(String url, Map<String, String> form) throws IOException {
        String res = null;
        ResponseBody rBody = postBody(url, form);
        if (rBody != null) {
            res = rBody.string();
            rBody.close();
        }
        return res;
    }

    public byte[] postBytes(String url, Map<String, String> form) throws IOException {
        byte[] res = null;
        ResponseBody rBody = postBody(url, form);
        if (rBody != null) {
            res = rBody.bytes();
            rBody.close();
        }
        return res;
    }

    public ResponseBody postBody(String url, Map<String, String> form) throws IOException {
        FormBody.Builder formBody = new FormBody.Builder();
        for (Map.Entry<String, String> entry : form.entrySet())
            formBody.add(entry.getKey(), entry.getValue());

        FormBody body = formBody.build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        ResponseBody res = null;
        if (response.isSuccessful()) {
            res = response.body();
        } else {
            throw new IOException("Unexpected code " + response);
        }
        return res;
    }

    // post multi part
    public String post(String url, Map<String, String> form, Map<String, byte[]> files) throws IOException {
        String res = null;
        ResponseBody rBody = postBody(url, form, files);
        if (rBody != null) {
            res = rBody.string();
            rBody.close();
        }
        return res;
    }

    public byte[] postBytes(String url, Map<String, String> form, Map<String, byte[]> files) throws IOException {
        byte[] res = null;
        ResponseBody rBody = postBody(url, form, files);
        if (rBody != null) {
            res = rBody.bytes();
            rBody.close();
        }
        return res;
    }

    public ResponseBody postBody(String url, Map<String, String> form, Map<String, byte[]> files) throws IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        for (Map.Entry<String, String> entry : form.entrySet())
            builder.addFormDataPart(entry.getKey(), entry.getValue());

        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            builder.addFormDataPart(entry.getKey(), null, RequestBody.create(BYTE, entry.getValue()));
        }
        MultipartBody body = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        ResponseBody res = null;
        if (response.isSuccessful()) {
            res = response.body();
        } else {
            throw new IOException("Unexpected code " + response);
        }
        return res;
    }

    public static OkHttpUtil getInstance(OkHttpClient okHttpClient) {
        if (mInstance == null) {
            synchronized (OkHttpUtil.class) {
                if (mInstance == null) {
                    mInstance = new OkHttpUtil(okHttpClient);
                }
            }
        }
        return mInstance;
    }

    public static OkHttpUtil getInstance() {
        return getInstance(null);
    }
}
