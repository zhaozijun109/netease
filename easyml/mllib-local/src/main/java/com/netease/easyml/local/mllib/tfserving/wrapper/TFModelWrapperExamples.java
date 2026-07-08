package com.netease.easyml.local.mllib.tfserving.wrapper;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.netease.easyml.local.mllib.tfserving.client.AsyncClientWrapper;
import com.netease.easyml.local.mllib.tfserving.config.ModelBaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.tensorflow.framework.TensorProto;
import tensorflow.serving.Predict;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * @author hejiecheng
 * @Date 2020-04-13
 */
@Slf4j
public class TFModelWrapperExamples extends AbstractTFModelWrapperExamples {
    /*
     *  用于处理单个分片的任务
     */
    protected static ExecutorService executorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder()
            .setNameFormat("wrapper-pool-%d").build());
    AsyncClientWrapper asyncClientWrapper;

    protected TFModelWrapperExamples(ModelBaseConfig config) {
        super(config);
        this.asyncClientWrapper = new AsyncClientWrapper(config);
    }

    public Map<String, Object> predict(Map<String, Map<String, Object>> featureMap, Optional<Map<String, Object>> commonFeatures) throws Exception {
        // 特征信息转List
        List<String> goodsId = new ArrayList<>();
        List<Map<String, Object>> featureList = new ArrayList<>();
        for (Map.Entry<String, Map<String, Object>> goodFeatures : featureMap.entrySet()) {
            goodsId.add(goodFeatures.getKey());
            featureList.add(goodFeatures.getValue());
        }
        FeatureBuilderExamples featureBuilderExamples = new FeatureBuilderExamples(config, commonFeatures);
        // 预测结果map
        ConcurrentMap<String, Object> predictRes = Maps.newConcurrentMap();
        // 构建CountDownLatch
        List<Batch> batches = batch(goodsId, featureList);
        int batchNum = batches.size();
        CountDownLatch countDownLatch = new CountDownLatch(batchNum);
        // 数据分片，提交任务
        for (int i = 0; i < batchNum; ++i) {
            Batch batch = batches.get(i);
            List<String> subGoodsId = batch.subGoodsId;
            List<Map<String, Object>> subFeature = batch.subFeature;
            SubFeatureTask task = new SubFeatureTask(
                    asyncClientWrapper,
                    subGoodsId,
                    subFeature,
                    featureBuilderExamples,
                    config,
                    predictRes,
                    countDownLatch,
                    i
            );
            executorService.submit(task);
        }
        // 等待结果
        boolean taskFinish = countDownLatch.await(config.getTimeOutMills(), TimeUnit.MILLISECONDS);
        if (!taskFinish) {
            // 在限制的时间内没有跑完
            throw new TimeoutException("predict timeout");
        }
//        if (goodsId.size() != predictRes.size()) {
//            // 结果数量不一致
//            throw new Exception("Different size from tf serving input:" + goodsId.size() + ", output:" + predictRes.size());
//        }
        return predictRes;
    }

    public static class SubFeatureTask implements Runnable {
        private final AsyncClientWrapper clientWrapper;
        private final List<String> subGoodsId;
        private final List<Map<String, Object>> subFeature;
        private final FeatureBuilderExamples featureBuilderExamples;
        private final ModelBaseConfig config;
        private final ConcurrentMap<String, Object> predictRes;
        private final CountDownLatch countDownLatch;
        private final Integer index;

        public SubFeatureTask(AsyncClientWrapper clientWrapper, List<String> subGoodsId, List<Map<String, Object>> subFeature,
                              FeatureBuilderExamples featureBuilderExamples, ModelBaseConfig config,
                              ConcurrentMap<String, Object> predictRes, CountDownLatch countDownLatch, Integer index) {
            this.clientWrapper = clientWrapper;
            this.subGoodsId = subGoodsId;
            this.subFeature = subFeature;
            this.featureBuilderExamples = featureBuilderExamples;
            this.config = config;
            this.predictRes = predictRes;
            this.countDownLatch = countDownLatch;
            this.index = index;
        }

        @Override
        public void run() {
            try {
                TensorProto tensor = featureBuilderExamples.buildFeatureProto(subFeature).build();
                ListenableFuture<Predict.PredictResponse> future = clientWrapper.requestAsync(tensor, this.index);
                Predict.PredictResponse response = null;
                try {
                    response = future.get(config.getTimeOutMills(), TimeUnit.MILLISECONDS);
                } catch (ExecutionException e) {
                    log.error("tf server send request error", e);
                    // 将线程任务取消，防止线程占用资源
                    future.cancel(true);
                    return;
                }
                List<Object> resList;
                if (config.isReturnAll()) {
                    Map<String, List<Object>> allScores = clientWrapper.getAllScores(response);
                    updateResults(predictRes, subGoodsId, allScores);
                } else {
                    resList = clientWrapper.getScores(response);
                    if (resList == null || subGoodsId.size() != resList.size()) {
                        log.error("tf server request different size");
                        return;
                    }
                    for (int i = 0; i < subGoodsId.size(); ++i) {
                        predictRes.put(subGoodsId.get(i), resList.get(i));
                    }
                }
            } catch (IOException e) {
                log.error("tf server send request error", e);
            } catch (InterruptedException | TimeoutException e) {
                log.error("tf server get response error", e);
            } catch (Exception e) {
                log.error("tf build error.", e);
            } finally {
                countDownLatch.countDown();
            }
        }

    }
}
