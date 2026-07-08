package com.netease.yuanqi.common.utils.filesystem;

import java.io.IOException;
import org.apache.flink.api.common.serialization.BulkWriter;
import org.apache.flink.api.common.serialization.Encoder;
import org.apache.flink.core.fs.FSDataOutputStream;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;

@Deprecated
public class TextCompressWriter<T> implements BulkWriter.Factory<T> {
    private final CompressionCodec hadoopCompressionCodec;
    private final Encoder<T> encoder;

    public TextCompressWriter(Encoder<T> encoder, CompressionCodecName compressionCodecName) {
        this.hadoopCompressionCodec =
                new CompressionCodecFactory(new Configuration())
                        .getCodecByName(compressionCodecName.name());
        this.encoder = encoder;
    }

    @Override
    public BulkWriter<T> create(FSDataOutputStream fsDataOutputStream) throws IOException {
        return new BulkWriter<T>() {
            private final CompressionOutputStream compressionOutputStream =
                    hadoopCompressionCodec.createOutputStream(fsDataOutputStream);

            @Override
            public void addElement(T t) throws IOException {
                encoder.encode(t, compressionOutputStream);
            }

            @Override
            public void flush() throws IOException {
                compressionOutputStream.flush();
                fsDataOutputStream.flush();
            }

            @Override
            public void finish() throws IOException {
                compressionOutputStream.finish();
                fsDataOutputStream.sync();
            }
        };
    }
}
