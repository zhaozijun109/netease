package com.netease.yuanqi.common.udf.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netease.yuanqi.common.pojo.model.ModelResult;
import com.netease.yuanqi.common.utils.model.CategoryModelContextTool;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.functions.AsyncScalarFunction;
import org.apache.flink.table.functions.FunctionContext;

public class CategoryModel extends AsyncScalarFunction {
    private final List<String> imgTypes =
            new ArrayList<>(
                    Arrays.asList(
                            "插画", "漫画", "cosplay", "memes", "捡手机", "明星美图", "剧照", "草图", "游戏动画截图",
                            "其他"));
    private final List<String> contentTypes =
            new ArrayList<>(Arrays.asList("连载", "短篇", "种草", "创粉沟通", "其他"));
    private ObjectMapper objectMapper;
    private OkHttpClient okHttpClient;
    private ThreadPoolExecutor asyncExecutor;

    @Override
    public void open(FunctionContext context) throws Exception {
        objectMapper = new ObjectMapper();
        okHttpClient =
                new OkHttpClient.Builder()
                        .connectTimeout(10, TimeUnit.SECONDS)
                        .readTimeout(10, TimeUnit.SECONDS)
                        .writeTimeout(10, TimeUnit.SECONDS)
                        .connectionPool(new ConnectionPool(15, 100, TimeUnit.SECONDS)) // 扩大连接数
                        .build();

        asyncExecutor =
                new ThreadPoolExecutor(
                        10,
                        10,
                        10,
                        TimeUnit.SECONDS,
                        new ArrayBlockingQueue<>(1000),
                        new ThreadPoolExecutor.CallerRunsPolicy());
    }

    /**
     * @param title 标题
     * @param content 内容
     * @param img 图片
     * @param tags 标签
     * @return String
     */
    public void eval(
            CompletableFuture<String> resultFuture,
            String title,
            String content,
            String img,
            String tags) {

        asyncExecutor.submit(
                () -> {
                    try {
                        final String res;
                        if (StringUtils.isNotBlank(img)) {
                            // 图片文章
                            res = calImgArticle(img, tags);
                        } else {
                            res = calContentArticle(title, content, tags);
                        }
                        resultFuture.complete(res);
                    } catch (Exception e) {
                        resultFuture.completeExceptionally(e);
                    }
                });
    }

    /**
     * @param contentModelName 文本模型名称
     * @param imgModelName 图片模型名称
     * @param modelTemperature 模型温度
     * @param modelKey 模型key
     * @param modelUrl 模型url
     * @param titlePrompt 标题prompt
     * @param contentPrompt 内容prompt
     * @param imgPrompt 图片prompt
     * @param title 标题
     * @param content 内容
     * @param img 图片
     * @param tags 标签
     * @param retryTime 重试次数
     * @return String
     */
    public void eval(
            CompletableFuture<String> resultFuture,
            String contentModelName,
            String imgModelName,
            double modelTemperature,
            String modelKey,
            String modelUrl,
            String titlePrompt,
            String contentPrompt,
            String imgPrompt,
            String title,
            String content,
            String img,
            String tags,
            int retryTime) {
        asyncExecutor.submit(
                () -> {
                    if (StringUtils.isNotBlank(img)) {
                        // 图片文章
                        resultFuture.complete(
                                calImgArticle(
                                        img,
                                        tags,
                                        imgPrompt,
                                        modelUrl,
                                        modelKey,
                                        imgModelName,
                                        modelTemperature,
                                        retryTime));
                    }
                    resultFuture.complete(
                            calContentArticle(
                                    title,
                                    content,
                                    tags,
                                    modelUrl,
                                    modelKey,
                                    contentModelName,
                                    modelTemperature,
                                    contentPrompt,
                                    titlePrompt,
                                    retryTime));
                });
    }

    private String calContentArticle(
            String title,
            String content,
            String tags,
            String modelUrl,
            String modelKey,
            String contentModelName,
            double modelTemperature,
            String contentPrompt,
            String titlePrompt,
            int retryTime) {
        // 先做原创标签匹配
        if (StringUtils.isNotBlank(tags)) {
            if (Arrays.stream(tags.split(","))
                    .anyMatch(CategoryModelContextTool.NOVAL_SET::contains)) {
                return ModelResult.success("原创小说", 1., objectMapper);
            }
        }
        // 标题短篇匹配
        if (StringUtils.isNotBlank(title)) {
            if (title.indexOf("一发完") > 0) {
                return ModelResult.success("短篇", 1., objectMapper);
            }
            // llm 识别标题连载
            Request titleReq =
                    new Request.Builder()
                            .url(modelUrl)
                            .headers(CategoryModelContextTool.buildHeaders(modelKey))
                            .post(
                                    RequestBody.create(
                                            MediaType.parse("application/json; charset=utf-8"),
                                            CategoryModelContextTool.buildBody(
                                                    contentModelName,
                                                    modelTemperature,
                                                    titlePrompt + title,
                                                    objectMapper)))
                            .build();
            ModelResult titleRes = doCall(titleReq, retryTime);
            if ("是".equals(titleRes.getResult())) {
                return ModelResult.success("连载", 1., objectMapper);
            }
        }
        // llm 识别正文
        if (content.length() > 2500) {
            content = content.substring(0, 1000) + content.substring(content.length() - 1000);
        }
        Request contentReq =
                new Request.Builder()
                        .url(modelUrl)
                        .headers(CategoryModelContextTool.buildHeaders(modelKey))
                        .post(
                                RequestBody.create(
                                        MediaType.parse("application/json; charset=utf-8"),
                                        CategoryModelContextTool.buildBody(
                                                contentModelName,
                                                modelTemperature,
                                                contentPrompt
                                                        + "标题："
                                                        + title
                                                        + "\n标签："
                                                        + tags
                                                        + "\n正文："
                                                        + content,
                                                objectMapper)))
                        .build();
        ModelResult contentRes = doCall(contentReq, retryTime);

        if (contentRes.getOriginalErr() == null
                && contentRes.getAnalyzedErr() == null
                && !contentTypes.contains(contentRes.getResult())) {
            contentRes = ModelResult.success("其他", contentRes.getProbability());
        }
        try {
            return objectMapper.writeValueAsString(contentRes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String calContentArticle(String title, String content, String tags) {
        return calContentArticle(
                title,
                content,
                tags,
                CategoryModelContextTool.MODEL_URL,
                CategoryModelContextTool.MODEL_KEY,
                CategoryModelContextTool.CONTENT_MODEL_NAME,
                CategoryModelContextTool.MODEL_TEMPERATURE,
                CategoryModelContextTool.CONTENT_PROMPT,
                CategoryModelContextTool.TITLE_PROMPT,
                3);
    }

    private String calImgArticle(
            String img,
            String tags,
            String imgPrompt,
            String modelUrl,
            String modelKey,
            String imgModelName,
            double modelTemperature,
            int retryTime) {
        // 标签先匹配
        if (StringUtils.isNotBlank(tags)) {
            String[] split = tags.split(",");

            if (Arrays.stream(split).anyMatch(CategoryModelContextTool.MEMES_SET::contains)) {
                return ModelResult.success("memes", 1., objectMapper);
            }
            if (Arrays.stream(split).anyMatch(CategoryModelContextTool.CRAFT_SET::contains)) {
                return ModelResult.success("手工", 1., objectMapper);
            }
            if (Arrays.stream(split).anyMatch(CategoryModelContextTool.COS_SET::contains)) {
                return ModelResult.success("cosplay", 1., objectMapper);
            }
        }
        // llm
        List<Map<String, Object>> contentList = new ArrayList<>();
        Map<String, Object> textMap = new HashMap<>();
        textMap.put("type", "text");
        textMap.put("text", imgPrompt + tags);
        contentList.add(textMap);

        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("type", "image_url");
        Map<String, Object> imageUrlMap = new HashMap<>();
        imageUrlMap.put("url", img);
        imageMap.put("image_url", imageUrlMap);
        contentList.add(imageMap);

        Request request =
                new Request.Builder()
                        .url(modelUrl)
                        .headers(CategoryModelContextTool.buildHeaders(modelKey))
                        .post(
                                RequestBody.create(
                                        MediaType.parse("application/json; charset=utf-8"),
                                        CategoryModelContextTool.buildBody(
                                                imgModelName,
                                                modelTemperature,
                                                contentList,
                                                objectMapper)))
                        .build();

        ModelResult result = doCall(request, retryTime);
        if (result.getOriginalErr() == null
                && result.getAnalyzedErr() == null
                && !imgTypes.contains(result.getResult())) {
            result = ModelResult.success("其他", result.getProbability());
        }
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private String calImgArticle(String img, String tags) {
        return calImgArticle(
                img,
                tags,
                CategoryModelContextTool.IMG_PROMPT,
                CategoryModelContextTool.MODEL_URL,
                CategoryModelContextTool.MODEL_KEY,
                CategoryModelContextTool.IMG_MODEL_NAME,
                CategoryModelContextTool.MODEL_TEMPERATURE,
                CategoryModelContextTool.RETRY_TIME);
    }

    private ModelResult doCall(Request request, int retryTime) {
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.body() == null) {
                if (retryTime > 0) {
                    return doCall(request, retryTime - 1);
                }
                return ModelResult.err("", "模型调用异常");
            }
            String reStr = response.body().string();
            if (response.code() == 200) {
                String data =
                        Optional.of(reStr)
                                .map(
                                        str -> {
                                            try {
                                                return objectMapper.readTree(reStr);
                                            } catch (JsonProcessingException e) {
                                                return null;
                                            }
                                        })
                                .map(node -> node.get("choices").get(0))
                                .map(node -> node.get("message"))
                                .map(node -> node.get("content").asText())
                                .orElse("");
                JsonNode jsonData = objectMapper.readTree(data);
                Double probability =
                        jsonData.get("probability") != null
                                ? jsonData.get("probability").asDouble()
                                : null;
                //                if (probability >= 0.6) {
                //                    return jsonData.getString("result");
                //                }
                //                // 结果不置信，重试
                //                return retryTime > 0 ? doCall(request, retryTime - 1) : "结果不置信: "
                // + probability + ",放弃,请人工处理";
                String result =
                        jsonData.get("result") != null ? jsonData.get("result").asText() : null;
                return ModelResult.success(result, probability);
            } else {
                if (retryTime > 0) {
                    return doCall(request, retryTime - 1);
                }
                String msg =
                        Optional.of(reStr)
                                .map(
                                        str -> {
                                            try {
                                                return (ObjectNode) objectMapper.readTree(str);
                                            } catch (JsonProcessingException e) {
                                                return null;
                                            }
                                        })
                                .map(
                                        node ->
                                                node.get("error") != null
                                                        ? node.get("error")
                                                        : objectMapper.createObjectNode())
                                .map(
                                        node ->
                                                node.get("message") != null
                                                        ? node.get("message").asText()
                                                        : null)
                                .orElse("");
                return parseErr(msg);
            }
        } catch (Exception e) {
            if (retryTime > 0) {
                return doCall(request, retryTime - 1);
            }
            return ModelResult.err(e.getMessage(), "服务内部异常");
        }
    }

    private ModelResult parseErr(String msg) {
        String ae = "";
        for (Map.Entry<String, String> entry : CategoryModelContextTool.ERR_TYPE_MAP.entrySet()) {
            if (msg.indexOf(entry.getKey()) > 0) {
                ae = entry.getValue();
                break;
            }
        }
        return ModelResult.err(msg, ae);
    }

    public String getName() {
        return "callModelCategory";
    }
}
