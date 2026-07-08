package com.linkedin.spark.datasources.tfrecordv2

import java.io._

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, KryoSerializable}
import com.linkedin.spark.datasources.tfrecord.{DefaultSource => DefaultSource_}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.FileStatus
import org.apache.hadoop.io.SequenceFile.CompressionType
import org.apache.hadoop.mapreduce.{Job, TaskAttemptContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.execution.datasources._
import org.apache.spark.sql.sources.Filter
import org.apache.spark.sql.types._
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

class DefaultSource extends DefaultSource_ {
  override val shortName: String = "tfrecordv2"

  override def inferSchema(sparkSession: SparkSession,
                           options: Map[String, String],
                           files: Seq[FileStatus]): Option[StructType] = {
    val keys = options.getOrElse("keys", "").split("[,;:]").map(_.trim).filterNot(_.equals("*"))
    val bytesKeys = options.getOrElse("bytes_keys", "").split("[,;:]").map(_.trim).filterNot(_.equals("*"))
    super.inferSchema(sparkSession, options, files).map(it => {
      var structType = if (keys.nonEmpty) {
        StructType(it.fields.filter(it => keys.contains(it.name)))
      } else {
        it
      }
      if (bytesKeys.nonEmpty) {
        structType = StructType(structType.fields.map(it => if (bytesKeys.contains(it.name)) {
          StructField(it.name, if (it.dataType == StringType) {
            BinaryType
          } else {
            ArrayType(BinaryType)
          }, it.nullable)
        }
        else it
        ))
      }
      structType
    })
  }

  override def prepareWrite(sparkSession: SparkSession,
                            job: Job,
                            options: Map[String, String],
                            dataSchema: StructType): OutputWriterFactory = {
    val conf = job.getConfiguration
    val codec = options.getOrElse("codec", "")
    if (!codec.isEmpty) {
      conf.set("mapreduce.output.fileoutputformat.compress", "true")
      conf.set("mapreduce.output.fileoutputformat.compress.type", CompressionType.BLOCK.toString)
      conf.set("mapreduce.output.fileoutputformat.compress.codec", codec)
      conf.set("mapreduce.map.output.compress", "true")
      conf.set("mapreduce.map.output.compress.codec", codec)
    }

    new OutputWriterFactory {
      override def newInstance(path: String,
                               dataSchema: StructType,
                               context: TaskAttemptContext): OutputWriter = {
        new TFRecordOutputWriter(path, options, dataSchema, context)
      }

      override def getFileExtension(context: TaskAttemptContext): String = {
        ".tfrecord" + CodecStreams.getCompressionExtension(context)
      }
    }
  }

  override def equals(other: Any): Boolean = other.isInstanceOf[DefaultSource]

  override def buildReader(sparkSession: SparkSession,
                           dataSchema: StructType,
                           partitionSchema: StructType,
                           requiredSchema: StructType,
                           filters: Seq[Filter],
                           options: Map[String, String],
                           hadoopConf: Configuration): PartitionedFile => Iterator[InternalRow] = {
    val broadcastedHadoopConf =
      sparkSession.sparkContext.broadcast(new SerializableConfiguration(hadoopConf))

    (file: PartitionedFile) => {
      TFRecordFileReader.readFile(
        broadcastedHadoopConf.value.value,
        options,
        file,
        requiredSchema)
    }
  }
}

class SerializableConfiguration(@transient var value: Configuration)
  extends Serializable with KryoSerializable {
  @transient private lazy val log = LoggerFactory.getLogger(getClass)

  private def writeObject(out: ObjectOutputStream): Unit = tryOrIOException {
    out.defaultWriteObject()
    value.write(out)
  }

  private def readObject(in: ObjectInputStream): Unit = tryOrIOException {
    value = new Configuration(false)
    value.readFields(in)
  }

  private def tryOrIOException[T](block: => T): T = {
    try {
      block
    } catch {
      case e: IOException =>
        log.error("Exception encountered", e)
        throw e
      case NonFatal(e) =>
        log.error("Exception encountered", e)
        throw new IOException(e)
    }
  }

  def write(kryo: Kryo, out: Output): Unit = {
    val dos = new DataOutputStream(out)
    value.write(dos)
    dos.flush()
  }

  def read(kryo: Kryo, in: Input): Unit = {
    value = new Configuration(false)
    value.readFields(new DataInputStream(in))
  }
}


