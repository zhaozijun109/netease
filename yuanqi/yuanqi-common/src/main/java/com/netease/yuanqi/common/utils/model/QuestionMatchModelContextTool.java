package com.netease.yuanqi.common.utils.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import okhttp3.Headers;

public class QuestionMatchModelContextTool {
    public static final String CHALLENGE_QUESTION_MATCH = "challenge_question_match";
    public static final String MODEL_NAME = "qwen3.5-plus";
    public static final double MODEL_TEMPERATURE = 0.1;
    public static final double MODEL_TOP_P = 0.8; // 多样性
    public static final boolean THINKING = false; // 思考模式
    public static final String MODEL_KEY = "sk-9c44f1e5576a4bdc991715f76ec38169";
    public static final String MODEL_URL =
            "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";
    public static final String PROMPT =
            "你是LOFTER平台的活动稿件相关性评审专家。你需要根据活动信息和稿件内容，判断该稿件与活动的相关性得分。\n"
                    + "\n"
                    + "【重要规则】\n"
                    + "- 禁止通过标签(tag)判断相关性，仅根据稿件的实际内容进行评估。\n"
                    + "- 评估维度包括：模版相似度、主题契合度、内容表现力。\n"
                    + "\n"
                    + "## 评估要求\n"
                    + "### 1. 模版相似度（1-100分）\n"
                    + "- 90-100分：稿件高度符合模版要求，核心视觉特征完整体现\n"
                    + "- 70-89分：稿件较好地符合模版要求，大部分视觉特征体现\n"
                    + "- 40-69分：稿件部分符合模版要求，能看出模版影响但偏离明显\n"
                    + "- 20-39分：稿件与模版关联较弱\n"
                    + "- 1-19分：稿件与模版几乎无关\n"
                    + "\n"
                    + "### 2. 主题契合度（1-100分）\n"
                    + "- 90-100分：稿件内容与活动主题高度契合\n"
                    + "- 70-89分：契合度较高，有少量偏离\n"
                    + "- 40-69分：部分相关\n"
                    + "- 20-39分：关联较弱\n"
                    + "- 1-19分：几乎无关\n"
                    + "\n"
                    + "### 3. 内容表现力（1-100分）\n"
                    + "- 90-100分：创作质量极高，极具感染力和创意\n"
                    + "- 70-89分：创作质量较高\n"
                    + "- 40-69分：创作质量一般\n"
                    + "- 20-39分：创作质量较低\n"
                    + "- 1-19分：创作质量很低\n"
                    + "\n"
                    + "## 输出格式\n"
                    + "请严格按照以下JSON格式输出，不要输出任何其他内容，包括```json```：\n"
                    + "{\n"
                    + "    \"template_fitness\": 1-100,\n"
                    + "    \"theme_fit\": 1-100,\n"
                    + "    \"content_expressiveness\": 1-100,\n"
                    + "    \"content_direction\": \"内容方向（从候选列表中选择最匹配的，若无候选列表或均不匹配则为空字符串），不要带前缀序号\",\n"
                    + "    \"reason\": \"30字以内的综合评价理由\"\n"
                    + "}";

    public static final int RETRY_TIME = 3;

    public static Headers buildHeaders(String modelKey) {
        return new Headers.Builder()
                .add(
                        "Authorization",
                        modelKey != null && !modelKey.isEmpty() ? modelKey : MODEL_KEY)
                .add("Content-Type", "application/json")
                .build();
    }

    public static String buildBody(String modelName, Object input, ObjectMapper objectMapper) {
        try {
            Map<String, Object> data = new HashMap<>();
            Map<String, String> outputFormat = new HashMap<>();
            outputFormat.put("type", "json_object");
            List<Object> messages = new ArrayList<>();
            Map<String, Object> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", PROMPT);
            Map<String, Object> userMessage = new HashMap<>();
            userMessage.put("role", "user");
            userMessage.put("content", input);
            messages.add(systemMessage);
            messages.add(userMessage);
            data.put("model", modelName);
            data.put("temperature", MODEL_TEMPERATURE);
            data.put("top_p", MODEL_TOP_P);
            data.put("enable_thinking", THINKING);
            data.put("response_format", outputFormat);
            data.put("messages", messages);
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
