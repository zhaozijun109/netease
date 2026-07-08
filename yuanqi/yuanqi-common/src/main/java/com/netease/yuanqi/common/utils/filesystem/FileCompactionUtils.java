package com.netease.yuanqi.common.utils.filesystem;

import com.netease.yuanqi.common.pojo.avro.ods.ClientMdaLogAvro;
import com.netease.yuanqi.common.pojo.proto.ods.ClientMdaLogProtoBuilder;
import java.io.IOException;
import org.apache.avro.specific.SpecificData;
import org.apache.flink.annotation.Experimental;
import org.apache.flink.api.common.io.FileInputFormat;
import org.apache.flink.connector.file.sink.compactor.FileCompactStrategy;
import org.apache.flink.connector.file.sink.compactor.FileCompactor;
import org.apache.flink.connector.file.sink.compactor.InputFormatBasedReader;
import org.apache.flink.connector.file.sink.compactor.RecordWiseFileCompactor;
import org.apache.flink.core.fs.FileInputSplit;
import org.apache.flink.util.function.SerializableSupplierWithException;
import org.apache.hadoop.conf.Configuration;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.proto.ProtoParquetReader;

@Deprecated
@Experimental
public class FileCompactionUtils {

    public FileCompactionUtils() {}

    public static FileCompactStrategy createFileCompactStrategy() {
        return FileCompactStrategy.Builder.newBuilder()
                .setNumCompactThreads(6)
                .enableCompactionOnCheckpoint(3)
                .setSizeThreshold(1024)
                .build();
    }

    public static FileCompactor createFileCompactor() {
        return new RecordWiseFileCompactor<>(
                new InputFormatBasedReader.Factory<>(
                        new SerializableSupplierWithException<
                                FileInputFormat<ClientMdaLogAvro>, IOException>() {
                            @Override
                            public FileInputFormat<ClientMdaLogAvro> get() throws IOException {
                                return new AvroParquetInputFormatTest();
                            }
                        }));
    }

    private static class AvroParquetInputFormatTest extends FileInputFormat<ClientMdaLogAvro> {
        private transient ParquetReader<ClientMdaLogAvro> parquetReader;
        private transient ClientMdaLogAvro nextRecord;

        @Override
        public void open(FileInputSplit fileSplit) throws IOException {
            super.open(fileSplit);
            InputFile inputFile =
                    HadoopInputFile.fromPath(
                            new org.apache.hadoop.fs.Path(fileSplit.getPath().toUri()),
                            new Configuration());
            parquetReader =
                    AvroParquetReader.<ClientMdaLogAvro>builder(inputFile)
                            .withDataModel(SpecificData.get())
                            .build();
            nextRecord = parquetReader.read();
        }

        @Override
        public void close() throws IOException {
            super.close();
            parquetReader.close();
        }

        @Override
        public boolean reachedEnd() throws IOException {
            return nextRecord == null;
        }

        @Override
        public ClientMdaLogAvro nextRecord(ClientMdaLogAvro clientMdaLogAvro) throws IOException {
            ClientMdaLogAvro currentRecord = nextRecord;
            nextRecord = parquetReader.read();
            return currentRecord;
        }
    }

    private static class ProtobufParquetInputFormatTest
            extends FileInputFormat<ClientMdaLogProtoBuilder.ClientMdaLogProto> {
        private transient ParquetReader<ClientMdaLogProtoBuilder.ClientMdaLogProto> parquetReader;
        private transient ClientMdaLogProtoBuilder.ClientMdaLogProto nextRecord;

        @Override
        public void open(FileInputSplit fileSplit) throws IOException {
            super.open(fileSplit);
            InputFile inputFile =
                    HadoopInputFile.fromPath(
                            new org.apache.hadoop.fs.Path(fileSplit.getPath().toUri()),
                            new Configuration());
            parquetReader =
                    ProtoParquetReader.<ClientMdaLogProtoBuilder.ClientMdaLogProto>builder(
                                    inputFile)
                            .build();
            nextRecord = parquetReader.read();
        }

        @Override
        public void close() throws IOException {
            super.close();
            parquetReader.close();
        }

        @Override
        public boolean reachedEnd() throws IOException {
            return nextRecord == null;
        }

        @Override
        public ClientMdaLogProtoBuilder.ClientMdaLogProto nextRecord(
                ClientMdaLogProtoBuilder.ClientMdaLogProto clientMdaLogProto) throws IOException {
            ClientMdaLogProtoBuilder.ClientMdaLogProto currentRecord = nextRecord;
            nextRecord = parquetReader.read();
            return currentRecord;
        }
    }
}
