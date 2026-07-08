package org.apache.flink.connector.file.sink

import org.apache.flink.api.common.serialization.BulkWriter.Factory
import org.apache.flink.api.connector.sink.Sink
import org.apache.flink.connector.file.sink.FileSink.BulkFormatBuilder
import org.apache.flink.connector.file.sink.committer.FileCommitter
import org.apache.flink.connector.file.sink.writer.{FileWriter, FileWriterBucketFactory, FileWriterBucketState, FileWriterBucketStateSerializer}
import org.apache.flink.core.fs.Path
import org.apache.flink.core.io.SimpleVersionedSerializer
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.CheckpointRollingPolicy
import org.apache.flink.streaming.api.functions.sink.filesystem.{BucketAssigner, BucketWriter, BulkBucketWriter, OutputFileConfig}

class SlothBulkFormatBuilder[IN](basePath: Path, writerFactory: Factory[IN],
                                 assigner: BucketAssigner[IN, String], policy: CheckpointRollingPolicy[IN, String],
                                 bucketFactory: FileWriterBucketFactory[IN], outputFileConfig: OutputFileConfig,
                                 bucketCheckInterval: Long = 60000L)
  extends BulkFormatBuilder(basePath, bucketCheckInterval, writerFactory, assigner, policy, bucketFactory, outputFileConfig) {

  override def createWriter(context: Sink.InitContext): FileWriter[IN] = {
    val bucketWriter = createSlothBucketWriter()
    new FileWriter(this.basePath, assigner, this.bucketFactory, bucketWriter, policy, this.outputFileConfig, context.getProcessingTimeService, bucketCheckInterval)
  }

  override def createCommitter(): FileCommitter = {
    new FileCommitter(createSlothBucketWriter())
  }

  override def getCommittableSerializer: SimpleVersionedSerializer[FileSinkCommittable] = {
    val bucketWriter = createSlothBucketWriter()
    new FileSinkCommittableSerializer(bucketWriter.getProperties().getPendingFileRecoverableSerializer(), bucketWriter.getProperties().getInProgressFileRecoverableSerializer())
  }

  override def getWriterStateSerializer: SimpleVersionedSerializer[FileWriterBucketState] = {
    val bucketWriter = createSlothBucketWriter()
    new FileWriterBucketStateSerializer(bucketWriter.getProperties().getInProgressFileRecoverableSerializer(), bucketWriter.getProperties().getPendingFileRecoverableSerializer())
  }

  private def createSlothBucketWriter(): BucketWriter[IN, String] = {
    val fs = SlothHadoopFileSystem.getHadoopFileSystem
    new BulkBucketWriter[IN, String](fs.createRecoverableWriter, this.writerFactory)
  }
}
