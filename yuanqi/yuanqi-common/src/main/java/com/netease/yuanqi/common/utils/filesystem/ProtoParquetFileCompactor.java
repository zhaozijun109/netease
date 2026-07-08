package com.netease.yuanqi.common.utils.filesystem;

import com.netease.yuanqi.common.pojo.config.KerberosConfig;
import com.netease.yuanqi.common.pojo.proto.ods.ClientMdaLogProtoBuilder;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import org.apache.flink.annotation.Experimental;
import org.apache.flink.connector.file.sink.SlothHadoopFileSystem;
import org.apache.flink.connector.file.sink.compactor.OutputStreamBasedFileCompactor;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.core.fs.Path;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.util.HadoopInputFile;
import org.apache.parquet.io.InputFile;
import org.apache.parquet.proto.ProtoParquetReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
@Experimental
public class ProtoParquetFileCompactor extends OutputStreamBasedFileCompactor {
    private static final Logger LOG = LoggerFactory.getLogger(ProtoParquetFileCompactor.class);
    private final KerberosConfig kerberosConfig;

    public ProtoParquetFileCompactor(KerberosConfig kerberosConfig) {
        this.kerberosConfig = kerberosConfig;
    }

    @Override
    protected void doCompact(List<Path> inputFiles, OutputStream outputStream) throws Exception {
        FileSystem fs = SlothHadoopFileSystem.getSlothHadoopFileSystem(kerberosConfig);

        for (Path input : inputFiles) {
            InputFile inFile =
                    HadoopInputFile.fromPath(
                            new org.apache.hadoop.fs.Path(
                                    fs.getFileStatus(input).getPath().toUri()),
                            new org.apache.hadoop.conf.Configuration());
            ParquetReader<Object> parquetReader = ProtoParquetReader.builder(inFile).build();
            ClientMdaLogProtoBuilder.ClientMdaLogProto record;
            while ((record = (ClientMdaLogProtoBuilder.ClientMdaLogProto) parquetReader.read())
                    != null) {
                LOG.info("=============: {}", record);
                LOG.info("-------------: {}", Arrays.toString(record.toByteArray()));
                // TODO: only test compactor, the file can not be read.
                // outputStream.write(record.toString().getBytes(StandardCharsets.UTF_8));
                outputStream.write(record.toByteArray());
            }
            parquetReader.close();
            // fs.delete(input, true);
        }
    }

    /*
    @Override
    protected void doCompact(List<Path> inputFiles, OutputStream outputStream) throws Exception {
        FileSystem fs = SlothHadoopFileSystem.getSlothHadoopFileSystem(kerberosConfig);

        for (Path input : inputFiles) {
            FSDataInputStream fsDataInputStream = fs.open(input);
            Schema schema = new Schema.Parser().parse(fsDataInputStream);

            StreamFormat.Reader<ClientMdaLogAvro> reader =
                    AvroParquetReaders.forSpecificRecord(ClientMdaLogAvro.class)
                            .createReader(
                                    new Configuration(),
                                    fsDataInputStream,
                                    0,
                                    fs.getFileStatus(input).getLen());
            ClientMdaLogAvro record;
            while ((record = reader.read()) != null) {
                LOG.info("=============: {}", record);
                LOG.info("-------------: {}", Arrays.toString(record.toByteBuffer().array()));
                // TODO: only test compactor, the file can not be read.
                // outputStream.write(record.toString().getBytes(StandardCharsets.UTF_8));
                outputStream.write(record.toByteBuffer().array());
            }
            reader.close();
            // fs.delete(input, true);
        }

        //        InputFile inFile =
        //                HadoopInputFile.fromPath(
        //                        new org.apache.hadoop.fs.Path(file.toURI()), new
        // org.apache.hadoop.conf.Configuration());
        //
        //        ArrayList<T> results = new ArrayList<>();
        //        try (ParquetReader<T> reader =
        //
        // AvroParquetReader.<T>builder(inFile).withDataModel(dataModel).build()) {
        //            T next;
        //            while ((next = reader.read()) != null) {
        //                results.add(next);
        //            }
        //        }
    }
    */
}
