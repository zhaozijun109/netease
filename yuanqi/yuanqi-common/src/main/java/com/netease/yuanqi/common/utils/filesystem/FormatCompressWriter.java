package com.netease.yuanqi.common.utils.filesystem;

import com.google.protobuf.Message;
import com.netease.yuanqi.common.pojo.archive.ArchiveFormatRow;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.reflect.ReflectData;
import org.apache.avro.specific.SpecificData;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.flink.api.common.serialization.BulkWriter;
import org.apache.flink.api.common.serialization.Encoder;
import org.apache.flink.core.fs.FSDataOutputStream;
import org.apache.flink.formats.parquet.ParquetBuilder;
import org.apache.flink.formats.parquet.ParquetWriterFactory;
import org.apache.flink.formats.parquet.protobuf.ParquetProtoWriters;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.compress.CompressionCodec;
import org.apache.hadoop.io.compress.CompressionCodecFactory;
import org.apache.hadoop.io.compress.CompressionOutputStream;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.OutputFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormatCompressWriter {
    private static final Logger LOG = LoggerFactory.getLogger(FormatCompressWriter.class);

    public static <PROTO extends Message>
            ParquetWriterFactory<PROTO> forProtoParquetWriterWithReflectRecord(
                    Class<PROTO> type, CompressionCodecName compressionCodecName) {
        ParquetBuilder<PROTO> builder =
                (out) -> createProtoParquetWriter(type, out, compressionCodecName);
        return new ParquetWriterFactory<>(builder);
    }

    private static <PROTO extends Message> ParquetWriter<PROTO> createProtoParquetWriter(
            Class<PROTO> type, OutputFile out, CompressionCodecName compressionCodecName)
            throws IOException {
        return (new ParquetProtoWriters.ParquetProtoWriterBuilder<>(out, type))
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build();
    }

    public static <AVRO extends SpecificRecordBase>
            ParquetWriterFactory<AVRO> forAvroParquetWriterWithSpecificRecord(
                    Class<AVRO> type, CompressionCodecName compressionCodecName) {
        String schemaString = SpecificData.get().getSchema(type).toString();
        ParquetBuilder<AVRO> builder =
                (out) ->
                        createAvroParquetWriter(
                                schemaString, SpecificData.get(), out, compressionCodecName);
        return new ParquetWriterFactory<>(builder);
    }

    public static ParquetWriterFactory<GenericRecord> forAvroParquetWriterWithGenericRecord(
            Schema schema, CompressionCodecName compressionCodecName) {
        String schemaString = schema.toString();
        ParquetBuilder<GenericRecord> builder =
                (out) ->
                        createAvroParquetWriter(
                                schemaString, GenericData.get(), out, compressionCodecName);
        return new ParquetWriterFactory<>(builder);
    }

    public static <AVRO> ParquetWriterFactory<AVRO> forAvroParquetWriterWithReflectRecord(
            Class<AVRO> type, CompressionCodecName compressionCodecName) {
        String schemaString = ReflectData.get().getSchema(type).toString();
        ParquetBuilder<AVRO> builder =
                (out) ->
                        createAvroParquetWriter(
                                schemaString, ReflectData.get(), out, compressionCodecName);
        return new ParquetWriterFactory<>(builder);
    }

    private static <AVRO> ParquetWriter<AVRO> createAvroParquetWriter(
            String schemaString,
            GenericData dataModel,
            OutputFile out,
            CompressionCodecName compressionCodecName)
            throws IOException {
        Schema schema = (new Schema.Parser()).parse(schemaString);
        return AvroParquetWriter.<AVRO>builder(out)
                .withSchema(schema)
                .withDataModel(dataModel)
                .withCompressionCodec(compressionCodecName)
                .withWriteMode(ParquetFileWriter.Mode.OVERWRITE)
                .build();
    }

    public static BulkWriter.Factory<ArchiveFormatRow> forArchiveFormatRowBulkWriter(
            CompressionCodecName compressionCodecName) {
        return new BulkWriter.Factory<ArchiveFormatRow>() {
            @Override
            public BulkWriter<ArchiveFormatRow> create(FSDataOutputStream fsDataOutputStream)
                    throws IOException {
                CompressionCodec compressionCodec =
                        new CompressionCodecFactory(new Configuration())
                                .getCodecByName(compressionCodecName.name());
                CompressionOutputStream compressionOutputStream =
                        compressionCodec.createOutputStream(fsDataOutputStream);

                return new BulkWriter<ArchiveFormatRow>() {
                    @Override
                    public void addElement(ArchiveFormatRow archiveFormatRow) throws IOException {
                        compressionOutputStream.write(
                                archiveFormatRow.getData().getBytes(StandardCharsets.UTF_8));
                        compressionOutputStream.write(10); // \n
                    }

                    @Override
                    public void flush() throws IOException {
                        compressionOutputStream.flush();
                    }

                    @Override
                    public void finish() throws IOException {
                        compressionOutputStream.finish();
                    }
                };
            }
        };
    }

    public static Encoder<ArchiveFormatRow> forArchiveFormatRowEncoder() {
        return new Encoder<ArchiveFormatRow>() {
            @Override
            public void encode(ArchiveFormatRow archiveFormatRow, OutputStream outputStream)
                    throws IOException {
                outputStream.write(archiveFormatRow.getData().getBytes(StandardCharsets.UTF_8));
                outputStream.write(10);
            }
        };
    }

    private FormatCompressWriter() {}
}
