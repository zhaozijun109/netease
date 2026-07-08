package com.netease.yuanqi.common.utils.filesystem;

import com.google.protobuf.Message;
import java.io.IOException;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.flink.formats.parquet.ParquetBuilder;
import org.apache.flink.formats.parquet.ParquetWriterFactory;
import org.apache.flink.formats.parquet.protobuf.ParquetProtoWriters;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.OutputFile;

@Deprecated
public class ParquetCompressWriter {
    public static <T extends Message> ParquetWriterFactory<T> forType(
            Class<T> type, CompressionCodecName compressionCodecName) {
        ParquetBuilder<T> builder =
                (out) -> createParquetProtoWriter(type, out, compressionCodecName);
        return new ParquetWriterFactory<>(builder);
    }

    private static <T extends Message> ParquetWriter<T> createParquetProtoWriter(
            Class<T> type, OutputFile out, CompressionCodecName compressionCodecName)
            throws IOException {
        return (new ParquetProtoWriters.ParquetProtoWriterBuilder<>(out, type))
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build();
    }

    public static <T extends SpecificRecordBase> ParquetWriterFactory<T> forSpecificRecord(
            Class<T> type, CompressionCodecName compressionCodecName) {
        String schemaString = SpecificData.get().getSchema(type).toString();
        ParquetBuilder<T> builder =
                (out) ->
                        createParquetAvroWriter(
                                schemaString, SpecificData.get(), out, compressionCodecName);
        return new ParquetWriterFactory<>(builder);
    }

    public static ParquetWriterFactory<GenericRecord> forGenericRecord(
            Schema schema, CompressionCodecName compressionCodecName) {
        String schemaString = schema.toString();
        ParquetBuilder<GenericRecord> builder =
                (out) ->
                        createParquetAvroWriter(
                                schemaString, GenericData.get(), out, compressionCodecName);
        return new ParquetWriterFactory<>(builder);
    }

    public static <T> ParquetWriterFactory<T> forReflectRecord(
            Class<T> type, CompressionCodecName compressionCodecName) {
        String schemaString = ReflectData.get().getSchema(type).toString();
        ParquetBuilder<T> builder =
                (out) ->
                        createParquetAvroWriter(
                                schemaString, ReflectData.get(), out, compressionCodecName);
        return new ParquetWriterFactory<>(builder);
    }

    private static <T> ParquetWriter<T> createParquetAvroWriter(
            String schemaString,
            GenericData dataModel,
            OutputFile out,
            CompressionCodecName compressionCodecName)
            throws IOException {
        Schema schema = (new Schema.Parser()).parse(schemaString);
        return AvroParquetWriter.<T>builder(out)
                .withSchema(schema)
                .withDataModel(dataModel)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build();
    }

    private static <T> ParquetWriter<T> createParquetTextWriter(
            String schemaString,
            GenericData dataModel,
            OutputFile out,
            CompressionCodecName compressionCodecName)
            throws IOException {
        Schema schema = (new Schema.Parser()).parse(schemaString);
        return AvroParquetWriter.<T>builder(out)
                .withSchema(schema)
                .withDataModel(dataModel)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build();
    }

    private ParquetCompressWriter() {}
}
