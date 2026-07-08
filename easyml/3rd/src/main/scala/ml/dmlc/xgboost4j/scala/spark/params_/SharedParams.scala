package ml.dmlc.xgboost4j.scala.spark.params_

import ml.dmlc.xgboost4j.scala.spark.params.{BoosterParams => OldBoosterParams, GeneralParams => OldGeneralParams, LearningTaskParams => OldLearningTaskParams}

/**
 * Created by linjiuning on 2020/11/3.
 */
trait LearningTaskParams extends OldLearningTaskParams {}

trait GeneralParams extends OldGeneralParams {}

trait BoosterParams extends OldBoosterParams {}