package com.netease.util;

import org.apache.flink.streaming.connectors.kafka.partitioner.FlinkKafkaPartitioner;

import java.io.Serializable;

public class KafkaSinkPartitioner<T> extends FlinkKafkaPartitioner<T> implements Serializable {
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
