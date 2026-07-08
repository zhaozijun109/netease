package com.netease.yuanqi.common.pojo.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;

public class ModelResult implements Serializable {
    private String result; // 模型返回的结果
    private Double probability; // 置信程度 0~1
    private String originalErr; // 原始错误信息
    private String analyzedErr; // 分析后的错误信息
    private long timestamp = System.currentTimeMillis(); // 模型响应时间
    private String msg;
    private String businessType; // 业务类型 challenge_question_match:挑战话题匹配度

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }

    public String getOriginalErr() {
        return originalErr;
    }

    public void setOriginalErr(String originalErr) {
        this.originalErr = originalErr;
    }

    public String getAnalyzedErr() {
        return analyzedErr;
    }

    public void setAnalyzedErr(String analyzedErr) {
        this.analyzedErr = analyzedErr;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "{"
                + "result='"
                + result
                + '\''
                + ", probability="
                + probability
                + ", originalErr='"
                + originalErr
                + '\''
                + ", analyzedErr='"
                + analyzedErr
                + '\''
                + ", timestamp="
                + timestamp
                + ", msg='"
                + msg
                + '\''
                + '}';
    }

    public static String success(String result, Double probability, ObjectMapper objectMapper) {
        try {
            ModelResult modelResult = new ModelResult();
            modelResult.setResult(result);
            modelResult.setProbability(probability);
            modelResult.setMsg("success");
            return objectMapper.writeValueAsString(modelResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String err(String oe, String ae, ObjectMapper objectMapper) {
        try {
            ModelResult modelResult = new ModelResult();
            modelResult.setOriginalErr(oe);
            modelResult.setAnalyzedErr(ae);
            return objectMapper.writeValueAsString(modelResult);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static ModelResult success(String result, Double probability) {
        ModelResult modelResult = new ModelResult();
        modelResult.setResult(result);
        modelResult.setProbability(probability);
        modelResult.setMsg("success");
        return modelResult;
    }

    public static ModelResult err(String oe, String ae) {
        ModelResult modelResult = new ModelResult();
        modelResult.setOriginalErr(oe);
        modelResult.setAnalyzedErr(ae);
        return modelResult;
    }

    public static ModelResult success(String result, String businessType) {
        return result("success", result, businessType);
    }

    public static ModelResult error(String result, String businessType) {
        return result("error", result, businessType);
    }

    public static ModelResult failed(String result, String businessType) {
        return result("failed", result, businessType);
    }

    public static ModelResult result(String msg, String result, String businessType) {
        ModelResult modelResult = new ModelResult();
        modelResult.setResult(result);
        modelResult.setBusinessType(businessType);
        modelResult.setMsg(msg);
        return modelResult;
    }
}
