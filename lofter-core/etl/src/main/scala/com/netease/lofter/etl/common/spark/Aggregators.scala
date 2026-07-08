package com.netease.lofter.etl.common.spark

import com.twitter.algebird.{MonoidAggregator, Aggregator => ABAggregator}
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.expressions.Aggregator
import org.apache.spark.sql.{Encoder, TypedColumn}

/**
  * Created by hzxiaonaitong on 2017/3/28.
  */
object Aggregators {

  implicit class MonoidToTypedColumn[-A,B: Encoder,C: Encoder](val m: MonoidAggregator[A,B,C]) {
    def toColumn: TypedColumn[A,C] = new MonoidAggregatorAdaptor(m).toColumn
  }

  implicit class SemigroupToTypedColumn[-A,B: Encoder, C: Encoder](val s: ABAggregator[A,B,C]) {
    def toColumn: TypedColumn[A, C] = new SemigroupAggregatorAdaptor(s).toColumn
  }
  class MonoidAggregatorAdaptor[-A,B: Encoder,C: Encoder](val m: MonoidAggregator[A,B,C]) extends  Aggregator[A,B,C] {
    override def zero = m.monoid.zero
    override def reduce(b: B, a: A) = m.reduce(b, m.prepare(a))
    override def finish(reduction: B) =  m.present(reduction)
    override def merge(b1: B, b2: B) = m.reduce(b1, b2)

    override def bufferEncoder = implicitly[Encoder[B]]
    override def outputEncoder = implicitly[Encoder[C]]
  }

  class SemigroupAggregatorAdaptor[-A,B: Encoder, C: Encoder](s: ABAggregator[A,B,C])
    extends Aggregator[A, (Boolean, B), C] {

    private val encoder = implicitly[Encoder[B]]

    override def zero: (Boolean, B) = (false, null.asInstanceOf[B])

    override def bufferEncoder: Encoder[(Boolean, B)] =
      ExpressionEncoder.tuple(
        ExpressionEncoder[Boolean](),
        encoder.asInstanceOf[ExpressionEncoder[B]])

    override def outputEncoder: Encoder[C] = implicitly[Encoder[C]]

    override def reduce(b: (Boolean, B), a: A): (Boolean, B) = {
      if (b._1) {
        (true, s.reduce(b._2, s.prepare(a)))
      } else {
        (true, s.prepare(a))
      }
    }

    override def merge(b1: (Boolean, B), b2: (Boolean, B)): (Boolean, B) = {
      if (!b1._1) {
        b2
      } else if (!b2._1) {
        b1
      } else {
        (true, s.reduce(b1._2, b2._2))
      }
    }

    override def finish(reduction: (Boolean, B)): C = {
      if (!reduction._1) {
        throw new IllegalStateException("SemigroupAggregatorAdaptor requires at least one input row")
      }
      s.present(reduction._2)
    }
  }

}
