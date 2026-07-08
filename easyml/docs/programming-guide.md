# EasyML开发文档

EasyML是基于Spark开发的算法库，接口遵循Spark ML规范。



## 项目结构

EasyML基于Maven构建，由以下子工程组成

- assembly：生成发布包
- 3rd：第三方依赖，主要用于未在maven仓库中发布的开源代码（例如：crf4j）
- common：包含常用的工具类（例如：IOUtil，SparkUtil，StringUtil等）
- graphx：图算法（例如：RandomWalk）
- mllib：分布式机器学习算法（例如：分类算法FM，序列标注算法CRF，评估函数AccuracyScore等）
- mllib-local：单机算法（例如：分类算法Fasttext，分词器Hanlp等，相似度EditScorer等）
- examples：算法使用示例和toy数据集
- launcher：命令行+配置文件
- ndarray：矩阵计算



## 接口规范

EasyML的接口遵循Spark mllib规范，核心接口如下



### Estimator

基于训练数据，训练模型

```scala
abstract class Estimator[M <: Model[M]] extends PipelineStage {
  /**
   * Fits a model to the input data.
   */
  def fit(dataset: Dataset[_]): M
}
```



### Transformer

数据转换：包括数据处理，特征工程，模型预测等

```scala
abstract class Transformer extends PipelineStage {
  /**
   * Transforms the input dataset.
   */
  def transform(dataset: Dataset[_]): DataFrame
}
```



### Metric

评估函数，继承Spark Evaluator接口，Evaluator接口只支持Double返回值，Metric支持Map返回多个指标

```scala
abstract class Metric extends Evaluator {

  protected val shortName: String = this.getClass.getSimpleName.toLowerCase

  def evaluateJson(dataset: Dataset[_]): JParams = {
    val metric = evaluate(dataset)
    val jParams = new JParams()
    jParams.put(shortName, metric)
    jParams
  }
}
```



### DatasetReader

负责数据读取

```scala
abstract class DatasetReader extends Params {
  def read(spark: SparkSession, params: JParams): DataFrame
}
```



### DatasetWriter

负责数据写入

```scala
abstract class DatasetWriter extends Params {
  def write(dataFrame: DataFrame, params: JParams)
}
```



### SklearnReader

负责从pickle中读取模型参数，实例化spark算子

```scala
trait SklearnReader[T] {

  /**
   * Returns an Spark ML instance for this class.
   */
  def readPickle(pickle: ClassDict): T
}
```



### Trainer

负责调度训练流程

```scala
abstract class Trainer extends Params {

  def fit(trainDf: DataFrame): Unit

  def transform(testDf: DataFrame): DataFrame

  def fitTransform(trainDf: DataFrame): DataFrame

  def evaluate(evalDf: DataFrame): JParams

  def metric(predDf: DataFrame): JParams

  def load(path: String): Unit

  def save(path: String): Unit
}
```



### UDScript

脚本接口规范，必须为**object**类型，类名必须以**UDScript**结尾，**run**方法签名必须一致

```scala
object MyUDScript {
  def run(spark: SparkSession, args: Array[String]): Unit
}
```



## 注解

EasyML支持用Json文件指定模型参数，定制机器学习Pipeline，结合命令行进行模型训练&预测&评估。组件的注册和实例化可通过注解定制。



### Register

EasyML启动时会将继承自`PipelineStage`接口的类和带有`Register`注解的类注册到注册中心，不同组件有唯一ID（默认为类的SimpleClassName，或通过Register的name字段指定）。

- name：组件name，默认为SimpleClassName
- prefix：组件name前缀，默认为空
- alias：组件别名，可指定多个
- parent：指定父类
- existOk：是否覆盖已存在组件，默认false



### Alias

组件实例化时，指定字段别名



### Ignore

组件实例化时，指定字段忽略配置文件参数



## Git规范

1. 发起 Issue：在项目的 Issues 板块发起 issue 并 assign 项目组成员。

2. 新建分支：Issue 通过评审后，基于 Master 分支新建 dev 分支，并进行开发。

   dev分支命名规范：

   1. 新特性开发：分支以feat标识，例如feat/onnx
   2. BUG修复：分支以fix标识，例如fix/hotbug
   3. 文档更新：分支以docs标识，例如docs/api

3. 提交 Merge Request：在 Git 上提交 Merge Request。