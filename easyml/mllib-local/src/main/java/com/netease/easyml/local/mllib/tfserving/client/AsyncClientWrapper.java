package com.netease.easyml.local.mllib.tfserving.client;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ListenableFuture;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import com.netease.easyml.local.mllib.tfserving.wrapper.FeatureBuilderExamples;
import lombok.extern.slf4j.Slf4j;
import org.tensorflow.framework.TensorProto;
import tensorflow.serving.Predict;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author hejiecheng
 * @Date 2020-04-13
 */
@Slf4j
public class AsyncClientWrapper extends ClientWrapper {

    private static final Long TIME_OUT_MS = 500L;
    private Long timeoutMs;


    public AsyncClientWrapper(ModelBaseConfig config) {
        this(config, TIME_OUT_MS);
    }

    List<Map<Integer, Future<ListenableFuture<Predict.PredictResponse>>>> resultMapList = Lists.newLinkedList();

    public AsyncClientWrapper(ModelBaseConfig config, Long timeoutMs) {
        super(config);
        this.timeoutMs = timeoutMs;
    }

    public ListenableFuture<Predict.PredictResponse> requestAsync(TensorProto proto, Integer index) throws IOException {
        try {
            Predict.PredictRequest req = prepareRequest(proto);

            return retryOnceRequest(req, index);
        } catch (Exception e) {
            log.info(
                    "tf serving error:{} ,maybe invalid config model name={} , host={}, port={}",
                    e.getMessage(),
                    config.getModelName(),
                    config.getHost(),
                    config.getPort());
            throw new IOException("tf serving error", e);
        }
    }

    private ListenableFuture<Predict.PredictResponse> retryOnceRequest(Predict.PredictRequest req, Integer index) {
        TensorflowServiceGrpcClient localClient =
                TensorflowGrpcConnectionPool.INSTANCE.getConnection(config.getHost(), config.getPort(), index);
        ListenableFuture<Predict.PredictResponse> responseFuture = null;
        try {
            responseFuture = localClient.predictAsync(req);
        } catch (Exception ex) {
            log.error("predict error retry.", ex);
            responseFuture = localClient.predictAsync(req);
        }
        return responseFuture;
    }

}
