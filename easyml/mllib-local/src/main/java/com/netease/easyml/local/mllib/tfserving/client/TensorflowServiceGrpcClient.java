package com.netease.easyml.local.mllib.tfserving.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.Any;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.Data;
import tensorflow.serving.GetModelMetadata;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

import java.util.Map;

/**
 * @author hejiecheng
 * @Date 2020-04-13
 */

@Data
public class TensorflowServiceGrpcClient {

    private ManagedChannel channel;

    private PredictionServiceGrpc.PredictionServiceBlockingStub blockingStub;

    private PredictionServiceGrpc.PredictionServiceStub tensorStub;

    private PredictionServiceGrpc.PredictionServiceFutureStub futureStub;

    private String host;

    private int port;

    public TensorflowServiceGrpcClient(String host, Integer port) {

        channel = ManagedChannelBuilder.forAddress(host, port).enableRetry().usePlaintext().build();

        tensorStub = PredictionServiceGrpc.newStub(channel);

        blockingStub = PredictionServiceGrpc.newBlockingStub(channel);

        futureStub = PredictionServiceGrpc.newFutureStub(channel);
    }

    public ListenableFuture<Predict.PredictResponse> predictAsync(Predict.PredictRequest req) {
        return futureStub.predict(req);
    }


    public Predict.PredictResponse predict(Predict.PredictRequest req) {
        return blockingStub.predict(req);
    }

    public Map<String, Any> getMetaInfo(GetModelMetadata.GetModelMetadataRequest req) {
        GetModelMetadata.GetModelMetadataResponse response = blockingStub.getModelMetadata(req);
        return response.getMetadataMap();
    }

}
