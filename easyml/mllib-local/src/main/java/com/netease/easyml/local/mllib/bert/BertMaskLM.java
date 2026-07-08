package com.netease.easyml.local.mllib.bert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.netease.easyml.common.util.OkHttpUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static com.netease.easyml.local.mllib.bert.Constant.*;


/**
 * Created by eddielin on 2019/3/1.
 */
@Slf4j
public class BertMaskLM {
    @AllArgsConstructor
    @Data
    public static class Term {
        private String token;
        private double prob;
    }

    private String url;

    private OkHttpUtil client;

    public BertMaskLM(OkHttpUtil client, String url) {
        this.url = url;
        this.client = client;

        if (this.client == null)
            this.client = OkHttpUtil.getInstance();
    }

    public BertMaskLM(String url) {
        this(null, url);
    }

    public double[][] predict(String seq, String[] tokens) {
        return predict(new String[]{seq}, tokens)[0];
    }

    public double[][][] predict(String[] seqs, String[] tokens) {
        JSONArray array = predict(seqs, -1, tokens);
        if (array == null)
            return new double[seqs.length][0][0];
        double[][][] result = new double[seqs.length][][];
        for (int i = 0; i < result.length; i++) {
            JSONArray vec1 = array.getJSONArray(i);
            double[][] v1 = new double[vec1.size()][];
            for (int j = 0; j < v1.length; j++) {
                JSONArray vec2 = vec1.getJSONArray(j);
                double[] v2 = new double[vec2.size()];
                for (int k = 0; k < v2.length; k++)
                    v2[k] = vec2.getDouble(k);
                v1[j] = v2;
            }
            result[i] = v1;
        }
        return result;
    }

    public Term[][] predict(String seq, int top) {
        return predict(new String[]{seq}, top)[0];
    }

    public Term[][][] predict(String[] seqs, int top) {
        JSONArray array = predict(seqs, top, new String[0]);
        if (array == null)
            return new Term[0][][];
        Term[][][] result = new Term[seqs.length][][];
        for (int i = 0; i < array.size(); i++) {
            JSONArray jsonArray = array.getJSONArray(i);
            Term[][] sub = new Term[jsonArray.size()][];
            for (int j = 0; j < jsonArray.size(); j++) {
                JSONArray vec = jsonArray.getJSONArray(i);
                Term[] sub_ = new Term[vec.size()];
                for (int k = 0; k < vec.size(); k++) {
                    JSONObject obj = vec.getJSONObject(k);
                    String key = obj.getString(TOKEN);
                    double val = obj.getDoubleValue(PROB);
                    sub_[k] = new Term(key, val);
                }
                sub[j] = sub_;
            }
            result[i] = sub;
        }
        return result;
    }

    private JSONArray predict(String[] seqs, int top, String[] tokens) {
        if (seqs.length == 0)
            return null;
        JSONObject object = new JSONObject();
        JSONArray array = new JSONArray();
        for (String seq : seqs)
            array.add(seq);
        object.put(SEQS, array);
        if (top > 0)
            object.put(TOP, top);
        if (tokens.length > 0) {
            array = new JSONArray();
            for (String tk : tokens)
                array.add(tk);
            object.put(TOKENS, array);
        }
        String req = object.toJSONString();

        try {
            String resp = client.post(url, req);

            object = JSON.parseObject(resp);

            //TODO: log msg
            JSONArray data = object.getJSONArray(DATA);
            if (data == null || data.size() != seqs.length) {
                log.error("Data size must be equals seqs size...");
            } else
                return data;
        } catch (IOException e) {
            log.error("IOException: " + e.getMessage());
        }
        return null;
    }
}
