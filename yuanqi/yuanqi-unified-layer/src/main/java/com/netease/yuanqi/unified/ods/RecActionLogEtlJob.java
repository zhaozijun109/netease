package com.netease.yuanqi.unified.ods;

import com.netease.yuanqi.common.sink.kafka.KafkaRecSink;
import com.netease.yuanqi.common.source.kafka.KafkaCommonSource;
import com.netease.yuanqi.unified.operator.ods.rec.ActionDtoRichFlatMapFunction;
import com.netease.yuanqi.unified.operator.ods.rec.LofterClientMdaLogKeyedProcessFunction;
import com.netease.yuanqi.unified.operator.ods.rec.LofterClientMdaLogRichAsyncFunction;
import com.netease.yuanqi.unified.operator.ods.rec.LofterClientMdaLogRichFlatMapFunction;
import com.netease.yuanqi.unified.pojo.RecParsedLogEvents;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.CheckpointingOptions;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.StateBackendOptions;
import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.async.AsyncRetryStrategy;
import org.apache.flink.streaming.util.retryable.AsyncRetryStrategies;
import org.apache.flink.streaming.util.retryable.RetryPredicates;
import org.apache.flink.util.FlinkRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RecActionLogEtlJob {
    private static final Logger LOG = LoggerFactory.getLogger(RecActionLogEtlJob.class);

    private static void recActionLogEtlJob(ParameterTool params) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(params);
        env.setMaxParallelism(256);
        env.enableCheckpointing(120000);
        env.getConfig().enableObjectReuse();

        Configuration config = new Configuration();
        config.set(StateBackendOptions.STATE_BACKEND, "rocksdb");
        config.set(CheckpointingOptions.CHECKPOINT_STORAGE, "filesystem");
        config.set(CheckpointingOptions.INCREMENTAL_CHECKPOINTS, Boolean.TRUE);
        config.set(CheckpointingOptions.ENABLE_UNALIGNED, true);
        config.setString("execution.checkpointing.aligned-checkpoint-timeout", "2s");
        config.setString("taskmanager.network.memory.buffer-debloat.enabled", "true");
        env.configure(config);

        Properties properties = new Properties();
        properties.setProperty("partition.discovery.interval.ms", "120000"); // default 5min -> 2min
        properties.setProperty("compression.type", "lz4");

        DataStream<RecParsedLogEvents> lofterClientMdaLogStream =
                env.fromSource(
                                new KafkaCommonSource(
                                                "lofter.mda.online.json",
                                                "RecActionLogEtlJob",
                                                properties)
                                        .createLogSource(),
                                WatermarkStrategy.noWatermarks(),
                                "LofterClientMdaLogSource")
                        .setParallelism(32)
                        .uid("LofterClientMdaLogKafkaSource")
                        .name("LofterClientMdaLogKafkaSource")
                        .flatMap(new LofterClientMdaLogRichFlatMapFunction())
                        .setParallelism(32)
                        .uid("LofterClientMdaLogRichFlatMapFunction")
                        .name("LofterClientMdaLogRichFlatMapFunction")
                        .keyBy(
                                new KeySelector<RecParsedLogEvents, Integer>() {
                                    @Override
                                    public Integer getKey(RecParsedLogEvents recParsedLogEvents)
                                            throws Exception {
                                        return recParsedLogEvents.getDeviceUdid().hashCode();
                                    }
                                })
                        .process(new LofterClientMdaLogKeyedProcessFunction())
                        .setParallelism(32)
                        .uid("LofterClientMdaLogKeyedProcessFunction")
                        .name("LofterClientMdaLogKeyedProcessFunction");

        AsyncRetryStrategy<RecParsedLogEvents> asyncRetryStrategy =
                new AsyncRetryStrategies.FixedDelayRetryStrategyBuilder<RecParsedLogEvents>(
                                3, 1000L)
                        .ifResult(RetryPredicates.EMPTY_RESULT_PREDICATE)
                        .ifException(
                                RetryPredicates.createExceptionTypePredicate(
                                        FlinkRuntimeException.class))
                        .build();

        AsyncDataStream.unorderedWaitWithRetry(
                        lofterClientMdaLogStream,
                        new LofterClientMdaLogRichAsyncFunction(),
                        10,
                        TimeUnit.SECONDS,
                        20000,
                        asyncRetryStrategy)
                .setParallelism(32)
                .uid("LofterClientMdaLogRichAsyncFunction")
                .name("LofterClientMdaLogRichAsyncFunction")
                .disableChaining()
                .flatMap(new ActionDtoRichFlatMapFunction())
                .setParallelism(32)
                .uid("ActionDtoRichFlatMapFunction")
                .name("ActionDtoRichFlatMapFunction")
                .sinkTo(
                        new KafkaRecSink("rec_upload_action_parse", properties)
                                .createRecActionDtoSink())
                .setParallelism(32)
                .uid("LofterClientMdaLogKafkaSink")
                .name("LofterClientMdaLogKafkaSink");

        env.execute("Rec Action Log ETL Job");
    }

    public static void main(String[] args) throws Exception {
        recActionLogEtlJob(ParameterTool.fromArgs(args));
    }
}
