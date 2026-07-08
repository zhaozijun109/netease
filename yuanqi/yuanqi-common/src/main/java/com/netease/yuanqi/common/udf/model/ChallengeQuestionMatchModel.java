package com.netease.yuanqi.common.udf.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netease.yuanqi.common.pojo.model.ModelResult;
import com.netease.yuanqi.common.utils.model.QuestionMatchModelContextTool;
import java.io.IOException;
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
import org.apache.flink.table.functions.AsyncScalarFunction;
import org.apache.flink.table.functions.FunctionContext;

public class ChallengeQuestionMatchModel extends AsyncScalarFunction {

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
     * 这里注意只有挑战话题才进行这个模型的调用，其他的都不进行.
     *
     * @param userContent 大模型消息体
     * @return String
     */
    public void eval(CompletableFuture<String> resultFuture, String userContent) {
        asyncExecutor.submit(
                () -> {
                    try {
                        String res = calModel(userContent);
                        resultFuture.complete(res);
                    } catch (JsonProcessingException e) {
                        resultFuture.completeExceptionally(e);
                    }
                });
    }

    public String calModel(String userContent) throws JsonProcessingException {
        ModelResult result = null;
        try {
            String body =
                    QuestionMatchModelContextTool.buildBody(
                            QuestionMatchModelContextTool.MODEL_NAME,
                            objectMapper.readTree(userContent),
                            objectMapper);
            Request request =
                    new Request.Builder()
                            .url(QuestionMatchModelContextTool.MODEL_URL)
                            .headers(
                                    QuestionMatchModelContextTool.buildHeaders(
                                            QuestionMatchModelContextTool.MODEL_KEY))
                            .post(
                                    RequestBody.create(
                                            MediaType.parse("application/json; charset=utf-8"),
                                            body))
                            .build();
            result = doCall(request, QuestionMatchModelContextTool.RETRY_TIME);

        } catch (Exception e) {
            result =
                    ModelResult.error(
                            e.getMessage(), QuestionMatchModelContextTool.CHALLENGE_QUESTION_MATCH);
        }
        return objectMapper.writeValueAsString(result);
    }

    private ModelResult doCall(Request request, int retryTime) throws IOException {
        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.body() == null) {
                if (retryTime > 0) {
                    return doCall(request, retryTime - 1);
                }
                return ModelResult.failed(
                        "结果为null", QuestionMatchModelContextTool.CHALLENGE_QUESTION_MATCH);
            } else {
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
                    return ModelResult.success(
                            data, QuestionMatchModelContextTool.CHALLENGE_QUESTION_MATCH);
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
                    return ModelResult.failed(
                            msg, QuestionMatchModelContextTool.CHALLENGE_QUESTION_MATCH);
                }
            }
        } catch (Exception e) {
            if (retryTime > 0) {
                return doCall(request, retryTime - 1);
            }
            throw e;
        }
    }

    public String getName() {
        return "callChallengeQuestionModel";
    }
}
