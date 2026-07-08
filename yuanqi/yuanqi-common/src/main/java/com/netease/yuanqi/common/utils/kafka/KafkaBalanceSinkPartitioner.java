package com.netease.yuanqi.common.utils.kafka;

import java.io.Serializable;
import org.apache.flink.connector.kafka.sink.KafkaPartitioner;

public class KafkaBalanceSinkPartitioner<T> implements Serializable, KafkaPartitioner<T> {
    private static final long serialVersionUID = 1L;
    private int parallelInstanceId;
    private int parallelInstances;

    @Override
    public void open(int parallelInstanceId, int parallelInstances) {
        this.parallelInstanceId = parallelInstanceId;
        this.parallelInstances = parallelInstances;
    }

    @Override
    public int partition(T t, byte[] bytes, byte[] bytes1, String s, int[] partitions) {
        int partitionId =
                Math.abs(t.hashCode() % partitions.length) * parallelInstances + parallelInstanceId;
        return partitions[partitionId % partitions.length];
    }
}
