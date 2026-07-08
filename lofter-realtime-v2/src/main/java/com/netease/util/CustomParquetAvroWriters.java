package com.netease.util;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.flink.formats.parquet.ParquetBuilder;
import org.apache.flink.formats.parquet.ParquetWriterFactory;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.OutputFile;

import java.io.IOException;

public class CustomParquetAvroWriters {
    public static <T extends SpecificRecordBase> ParquetWriterFactory<T> forSpecificRecord(
            Class<T> type) {
        String schemaString = SpecificData.get().getSchema(type).toString();
        ParquetBuilder<T> builder =
                (out) -> {
                    return createAvroParquetWriter(schemaString, SpecificData.get(), out);
                };
        return new ParquetWriterFactory<>(builder);
    }

    public static ParquetWriterFactory<GenericRecord> forGenericRecord(Schema schema) {
        String schemaString = schema.toString();
        ParquetBuilder<GenericRecord> builder =
                (out) -> {
                    return createAvroParquetWriter(schemaString, GenericData.get(), out);
                };
        return new ParquetWriterFactory<>(builder);
    }

    public static <T> ParquetWriterFactory<T> forReflectRecord(Class<T> type) {
        String schemaString = ReflectData.get().getSchema(type).toString();
        ParquetBuilder<T> builder =
                (out) -> {
                    return createAvroParquetWriter(schemaString, ReflectData.get(), out);
                };
        return new ParquetWriterFactory<>(builder);
    }

    private static <T> ParquetWriter<T> createAvroParquetWriter(
            String schemaString, GenericData dataModel, OutputFile out) throws IOException {
        Schema schema = (new Schema.Parser()).parse(schemaString);

        return AvroParquetWriter.<T>builder(out)
                .withSchema(schema)
                .withDataModel(dataModel)
                .withCompressionCodec(CompressionCodecName.GZIP)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build();
    }

    public CustomParquetAvroWriters() {}
}
