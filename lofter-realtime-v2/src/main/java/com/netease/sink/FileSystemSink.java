package com.netease.sink;

import com.netease.util.CustomParquetAvroWriters;
import com.netease.util.FileSystemSinkUtils;

import org.apache.flink.connector.file.sink.FileSink;
import org.apache.flink.core.fs.Path;
import org.apache.flink.streaming.api.functions.sink.filesystem.bucketassigners.DateTimeBucketAssigner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemSink<T> {
    private static final Logger LOG = LoggerFactory.getLogger(FileSystemSink.class);
    private final Class<T> clazz;
    private final String path;
    private final String bucket;

    public FileSystemSink(Class<T> clazz, String path, String bucket) {
        this.clazz = clazz;
        this.path = path;
        this.bucket = bucket;
    }

    public FileSink<T> createSlothFileBucketSink() {
        return new FileSystemSinkUtils<>(
                        new Path(path),
                        CustomParquetAvroWriters.forReflectRecord(clazz),
                        new DateTimeBucketAssigner<>(bucket))
                .createSlothBulkFormatBuilder();
    }
}
