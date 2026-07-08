* This will become a table of contents (this text will be scraped).
{:toc}
# EasyML API文档

## 数据读取

数据读取模块主要用于配置文件

### BasicDatasetReader

从文件中读取数据

- 参数
  - **format**：数据格式，支持csv、json、parquet、jdbc、libsvm
  - **options**：配置，如csv指定delimiter、header等
  - **schema**：指定数据schema
  - **columns**：指定选择的column



### SQLDatasetReader

从Hive表中读取数据

- 参数

  - **sqls**: 从表中读取数据的sql语句

  - **columns**: 指定选择的column

    

### BasicDatasetWriter

预测结果写入文件

- 参数
  - **format**: 数据格式，支持csv、json、parquet、jdbc、text
  - **options**: 配置
  - **columns**: 指定选择的column
  - **mode**: 写入的模式，支持overwrite, append, ignore, error, errorifexists（默认）



### SQLDatasetWriter

预测结果写入Hive表

- 参数

  - **sqls**: 数据写入Hive相关的sql语句

  - **columns**: 指定选择的column

    

## 训练调度

### BasicTrainer

基本训练调度，支持交叉验证

- 参数

  - **split**：可选，指定**BaseCrossValidator**，例如ShuffleSplit，KFold

    

## 数据处理

### Coalesce

数据重分区，调用`dataset.coalesce`方法

- 参数

  - **numPartitions**：分区数

    

### Cast

数据转换

- 输入/输出：
  - **inputCol**：输入列名
  - **outputCol**：输出列名

- 参数

  - **to**：数据类型

    

### ColumnRenamed

列名重命名，调用`dataset.withColumnRenamed`方法

- 参数

  - **inputCols**：原列名，类型Array[String]

  - **outputCols**：新列名，类型Array[String]

    

### DropColumn

删除列，调用`dataset.drop`方法

- 参数
  - **inputCols**：要删除的列名，类型Array[String]

    

### Repartition

数据重分区，调用`dataset.repartition`方法

- 参数

  - **numPartitions**：分区数

    

### Sample

数据采样，调用`dataset.sample`方法

- 参数
  - **fraction**：采样比例
  - **withReplacement**：是否有放回采样，默认值false
  - **seed**：可选，随机数种子



### Select

数据列选取，调用`dataset.select`方法

- 参数

  - **inputCols**：要选取的列名，类型Array[String]

    

### Probability

选取预测label概率值

- 参数
  - **predictionCol**：模型预测label列名
  - **probabilityCol**：模型预测概率列名
  - **outputCol**：输出概率值列名



### ShuffleSplit

用于将样本集合随机“打散”后划分为训练集、测试集，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.model_selection.ShuffleSplit.html)。

- 参数
  - **nSplits**：划分训练集、测试集的次数，默认值10
  - **testSize**：测试集比例或样本数量，该值为[0.0, 1.0]内的浮点数时，表示测试集占总样本的比例；该值为整型值时，表示具体的测试集样本数量。若trainsize未指定，则默认值为0.1
  - **trainSize**：训练集比例或样本数量，该值为[0.0, 1.0]内的浮点数时，表示训练集占总样本的比例
  - **randomState**：可选，随机数种子

- 返回值

  - Iterator[(DataFrame, DataFrame)]：训练集，测试集划分结果迭代器

  

### KFold

将训练/测试数据集划分nSplits个互斥子集，每次用其中一个子集当作验证集，剩下的nSplits-1个作为训练集，进行nSplits次训练和测试，得到nSplits个结果，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.model_selection.KFold.html#sklearn.model_selection.KFold)。

- 参数
  - **nSplits**：划分训练集、测试集的次数，默认值5
  - **randomState**：可选，随机数种子
- 返回值
  - Iterator[(DataFrame, DataFrame)]：训练集，测试集划分结果迭代器



### RandomOverSampler

随机过采样

- 输入

  - **labelCol**：标签列，默认值label

- 参数

  - **samplingStrategy**：采样策略，默认值auto

    - `'minority'`: resample only the minority class;
    - `'not minority'`: resample all classes but the minority class;
    - `'not majority'`: resample all classes but the majority class;
    - `'all'`: resample all classes;
    - `'auto'`: equivalent to `'not majority'`.

  - **randomState**：可选，随机数种子

  - **samplingNum**：可选，指定采样数量
  
    

### RandomUnderSampler

随机欠采样

- 输入

  - **labelCol**：标签列，默认值label

- 参数

  - **samplingStrategy**：采样策略，默认值auto

    - `'majority'`: resample only the majority class;

    - `'not minority'`: resample all classes but the minority class;

    - `'not majority'`: resample all classes but the majority class;

    - `'all'`: resample all classes;

    - `'auto'`: equivalent to `'not minority'`.
- **randomState**：可选，随机数种子
  - **samplingNum**：可选，指定采样数量




## 模型预测

### CRFPredictor

读取CRF++训练模型，spark预测

- 输入/输出

  - **featuresCol**: String， crf特征，按separator分割

  - **rawPredictionCol**：Array[String]，每个token的预测label

  - **labelCol**：Array[Array[String]]，识别的实体

- 参数

  - **path**：模型路径

  - **separator**：可选，特征分隔符，默认按字分割

  - **labelMapPath**：可选，CRF标签映射关系文件路径

    

### FasttextPredictor

本地Fasttext训练模型，spark预测

- 输入/输出

  - **featuresCol**: String，文本，token按空格分隔

  - **predictionCol**：String，预测类目

  - **probabilityCol**：Double，类目概率

- 参数

  - **path**：模型路径

  - **index**：可选，是否先对token转为ID再进行预测，默认false

    

### ONNXPredictor

ONNX格式模型，spark预测

- 输入/输出
  
- **inputCols**: 输入特征
  
- 参数

  - **path**：模型路径

  - **logStep**：可选，记录预测耗时，默认128

  - **batchSize**：可选，默认为1




### MiaobiNLPPredictor

基于MiaobiNLP框架训练的Pytorch模型，Spark推断组件



## 预处理和归一化

### KBinsDiscretizer

连续特征离散化，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.preprocessing.KBinsDiscretizer.html#sklearn.preprocessing.KBinsDiscretizer)

- 输入/输出

  - **inputCol**：数据类型为Vector
  - **outputCol**：数据类型为Vector

- 参数

  - **nBins**：分桶数目，默认5

  - **encode**：分桶后编码方式，可选值onehot，onehot-dense，ordinal，默认onehot

  - **strategy**：分桶边界划分策略，可选值quantile，uniform，默认quantile

    

### KernalCenterer

详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.preprocessing.KernalCenterer.html#sklearn.preprocessing.KernalCenterer)

- 输入/输出
  - **inputCol**：数据类型为Vector

  - **outputCol**：数据类型为Vector

    

### LabelBinarizer

标签二值化，用于one-vs-all分类场景，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.preprocessing.LabelBinarizer.html#sklearn.preprocessing.LabelBinarizer)。

- 输入/输出

  - **inputCol**：数据类型为Numeric或String
  - **outputCol**：输出类型为Vector

- 参数

  - **negLabel**：默认0

  - **posLabel**：默认1

  - **sparseOutput**：默认false

    

### MultiLabelBinarizer

标签二值化，用于multilabel分类场景，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/classes.html#module-sklearn.preprocessing)。

- 输入/输出

  - **inputCol**：数据类型为Array[Any]
  - **outputCol**：输出类型为Vector

- 参数

  - **sparseOutput**：默认false

    

### Normalizer

向量归一化，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.preprocessing.Normalizer.html#sklearn.preprocessing.Normalizer)。

- 组件名：**sklearn.Normalizer**

- 输入/输出

  - **inputCol**：数据类型为Vector
  - **outputCol**：输出类型为Vector

- 参数

  - **norm**：可选值l1，l2，max，默认l2

    

### OneHotEncoder

将类目编码为OneHot向量，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.preprocessing.OneHotEncoder.html#sklearn.preprocessing.OneHotEncoder)。

- 组件名：**sklearn.OneHotEncoder**

- 输入/输出

  - **inputCol**：数据类型为String，Array[String]或Vector
  - **outputCol**：输出类型为Vector

- 参数

  - **handleUnknown**：可选值ignore，error，默认error

  - **drop**：可选值first，if_binary，none，默认none

  - **sparse**：是否输出稀疏向量，默认true

    

### OrdinalEncoder

将类目编码为数值，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.preprocessing.OrdinalEncoder.html#sklearn.preprocessing.OrdinalEncoder)。

- 输入/输出
  - **inputCol**：数据类型为String，Array[String]或Vector
  - **outputCol**：输出类型为Vector
- 参数
  - **handleUnknown**：可选值ignore，error，默认error



### RobustScaler

如果数据中含有异常值，那么使用均值和方差缩放数据的效果并不好。这种情况下，可以使用RobustScaler。详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.preprocessing.RobustScaler.html#sklearn.preprocessing.RobustScaler)。

- 输入/输出
  - **inputCol**：数据类型Vector
  - **outputCol**：数据类型Vector
- 参数
  - **withCentering**：在scaling之前对数据centering，默认true
  
  - **withScaling**：将数据scaling到quantileRange，默认true
  
  - **quantileRange**：默认(0.25, 0.75)
  
    

### Vectorizer

特征向量化

- 输入/输出

  - **inputCol**：数据类型String, Numeric, Array[Any], Map[Any, Any]
  - **outputCol**：数据类型Vector

- 参数

  - **sparse**: 是否输出稀疏向量，默认false
  - **handleUnknown**：可选值ignore，error，默认error
- **json**：指定输入是否是json字符串，默认false
  
  

### ArrayAssembler

同类型特征拼接成数组

- 输入/输出
  - **inputCols**：数据类型String, Numeric, Boolean
  - **outputCol**：数据类型Array[_]



## 特征提取

### CountVectorizer

将文本转为词频向量，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.feature_extraction.text.CountVectorizer.html#sklearn.feature_extraction.text.CountVectorizer)。

- 组件名：**sklearn.CountVectorizer**

- 输入/输出

  - **inputCol**：默认text，数据类型String
  - **outputCol**：默认features，数据类型Vector

- 参数

  - **binary**：是否输出0/1特征，默认false

  - **ngramRange**：指定ngram的[min_n, max_n]，默认[1, 1]，unigram

  - **lowercase**：是否转为小写，默认true

  - **tokenPattern**：分词正则，只有当`analyzer == word`时这个参数才有效，默认按空格分词

  - **analyzer**：指定字/词粒度ngram，可选值word，char，char_wb，默认word

  - **maxFeatures**：词表最大值，按照词频进行截断，默认2 ** 18

  - **minTF**：最小词频，默认1.0

  - **maxDf**：最大document frequency，默认 2 ** 18

  - **minDf**：最小document frequency，默认1.0

    

### HashingVectorizer

先将文本特征做哈希（相当于一种降维技巧），再转为词频向量，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.feature_extraction.text.HashingVectorizer.html#sklearn.feature_extraction.text.HashingVectorizer)。

- 组件名：**sklearn.HashingVectorizer**

- 输入/输出

  - **inputCol**：默认text，数据类型String

  - **outputCol**：默认features，数据类型Vector

- 参数

  - **binary**：是否输出为二值特征，默认false

  - **ngramRange**：指定ngram的[min_n, max_n]，默认[1, 1]，unigram

  - **lowercase**：是否转为小写，默认true

  - **tokenPattern**：分词正则，只有当`analyzer == word`时这个参数才有效，默认按空格分词

  - **analyzer**：指定字/词粒度ngram，可选值word，char，char_wb，默认word

  - **nFeatures**：特征数目，默认2 ** 20

  - **alternateSign**：是否保留正负值，默认true

    

### TfidfTransformer

基于**CountVectorizer**或**HashingVectorizer**的词频向量，计算Tfidf向量，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.feature_extraction.text.TfidfTransformer.html#sklearn.feature_extraction.text.TfidfTransformer)。

- 输入/输出

  - **inputCol**：默认text，数据类型String
  - **outputCol**：默认features，数据类型Vector

- 参数

  - **norm**：归一化方式，可选值l2，l1，默认l2

  - **minDocFreq**：最小文档频次，默认0

  - **useIdf**：默认true

  - **smoothIdf**：计算idf时是否做平滑，默认 true

  - **sublinearTf**：若为true，`tf = 1 + log(tf)`，默认false

    

### TfidfVectorizer

等价于**CountVectorizer** + **TfidfTransformer**，详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.feature_extraction.text.TfidfVectorizer.html#sklearn.feature_extraction.text.TfidfVectorizer)。



### NSWord2Vec

基于Gensim逻辑，Spark原生API实现的负采样Word2Vec训练，效率高于Spark原生Word2Vec



### PSNSWord2Vec

基于Gensim逻辑，Angel参数服务器API实现的负采样Word2Vec训练，支持至少千万级词表训练



### SIFDocVec

无监督DocVec实现，基于论文A Simple but Tough-to-Beat Baseline for Sentence *Embeddings*.



## 特征选择

### SelectFromModel

基于模型输出的特征重要度选择特征

- 输入/输出
  - **inputCols**：输入特征列名，若多于一个，会调用VectorAssembler合并
  - **featuresCol**：合并后的特征列名
  - **labelCol**：label列名
- 参数
  - **estimator**：模型，可选lr，xgb，lgb
  - **importanceType**：重要性类型，默认gain
  - **maxIter**：评估轮数，最后取重要性平均输出



### RFE

递归式特征消除，详细说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.feature_selection.RFE.html#sklearn.feature_selection.RFE)

- 输入/输出
  - **inputCols**：输入特征列名，若多于一个，会调用VectorAssembler合并
  - **featuresCol**：合并后的特征列名
  - **labelCol**：label列名
- 参数
  - **estimator**：模型，可选lr，xgb，lgb
  - **evaluator**：评估函数
  - **trainRatio**：训练集占比，默认0.75
  - **nFeaturesToSelect**：保留的特征个数，默认总特征数/2
  - **step**：每次迭代消除的特征个数，默认1



### RFECV

递归式特征消除，通过CV选取最优特征个数，详细说明查看[Sklearn文档]()

- 输入/输出
  - **inputCols**：输入特征列名，若多于一个，会调用VectorAssembler合并
  - **featuresCol**：合并后的特征列名
  - **labelCol**：label列名
- 参数
  - **estimator**：模型，可选lr，xgb，lgb
  - **evaluator**：评估函数
  - **cv**：交叉验证次数，默认3
  - **minFeaturesToSelect**：最少保留的特征个数，默认1
  - **step**：每次迭代消除的特征个数，默认1
  - **randomState**：随机数种子，默认90



## 评估指标

EasyML的评估函数继承自`Metric`接口，拓展了Spark `Evaluator`接口，支持Map返回多个指标。

评估函数包括：

1. Spark自带评估函数封装（例如，AreaUnderROC，MeanAbsoluteError）
2. [sklearn metrics](https://scikit-learn.org/stable/modules/classes.html#sklearn-metrics-metrics)分布式实现（例如，AccuracyScore，FBetaScore）



### 分类评估指标

#### AccuracyScore

计算分类准确率（默认）或计数 (normalize=False)。详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.accuracy_score.html#sklearn.metrics.accuracy_score)。

在 multilabel classification中，函数返回 subset accuracy。如果样本的 entire set of predicted labels 与真正的标签组合匹配，则子集精度为 1.0，否则为 0.0 。

- 别名：accuracy，acc

- 输入

  - **predictionCol**：预测类目，默认值"prediction"

    数据类型：

    ​	binary/multiclass：Double

    ​	multilabel：Array[Double]或Vector

  - **labelCol**：真实类目，默认值"label"，数据类型同predictionCol

  - **weightCol**：可选，样本权重，数据类型Double

- 参数

  - **normalize**：默认值true，返回准确率；若为false，返回正确分类样本个数



#### AreaUnderPR

二分类精度-召回曲线面积。

- 别名：areaUnderPR
- 参数
  - **rawPredictionCol**：预测正样本概率或概率分布，数据类型Double或Array[Double]
  - **labelCol**：真实类目，默认值"label"，数据类型Double



#### AreaUnderROC

二分类计算 receiver operating characteristic (ROC) 曲线下的面积

- 别名：auc，areaUnderRoc
- 参数
  - **rawPredictionCol**：预测正样本概率或概率分布，数据类型Double或Array[Double]
  - **labelCol**：真实类目，默认值"label"，数据类型Double



#### FBetaScore

计算F-beta指标，可以认为是precision和recall的加权调和平均。详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.fbeta_score.html#sklearn.metrics.fbeta_score)

通过 `beta`参数控制精确度（precision）和召回率（recall） 在计算F值时的比重。当 `beta < 1` 时精确度的比重更大，当 `beta > 1` 时召回率的比重更大。

- 别名：fscore，f-score，f_score

- 输入
  - **predictionCol**：预测类目，默认值"prediction"

    数据类型：

    ​	multiclass：Double

    ​	multilabel：Array[Double]或Vector

  - **labelCol**：真实类目，默认值"label"，数据类型同predictionCol

  - **weightCol**：可选，样本权重，数据类型Double

- 参数

  - **beta**：控制精确度和召回率在计算F值时的比重，默认值1

  - **average**：不同标签F值融合方式，默认值"none"

    - none：分别返回每个标签的F值

    - micro：通过先计算总体的TP，FN和FP的数量，再计算F1

    - macro：分布计算每个类别的F1，然后做平均（各类别F1的权重相同）

    - weighted：对每一类别的f_score进行加权平均



#### F1Score

计算F1值，也被称为平衡F-score或F-meature，参数同**FBetaScore**

- 别名：f1，f1-score，f1_score



#### PrecisionScore

计算精确度（precision），`precision = tp / (tp + fp)`。参数同**FBetaScore**



#### RecallScore

计算召回率（recall），`recall = tp / (tp + fn)`。参数同**FBetaScore**



#### ConfusionMatrix

计算binary/multiclass分类混淆矩阵，对于二分类，TN计数是C0,0，FN是C1,0，TP是C1,1，FP是C0,1。详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.confusion_matrix.html#sklearn.metrics.confusion_matrix)

- 别名：confusion_matrix

- 输入

  - **predictionCol**：预测类目，默认值"prediction"，数据类型Double
  - **labelCol**：真实类目，默认值"label"，数据类型同predictionCol
  - **weightCol**：可选，样本权重，数据类型Double

- 参数

  - **normalize**：归一化方式，默认值none

    - none：不归一

    - true：按行归一

    - pred：按列归一

    - all：全局归一

      

#### MultiLabelConfusionMatrix

计算多标签混淆矩阵，为每个标签单独计算混淆矩阵，输出N * 2 * 2矩阵。详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.multilabel_confusion_matrix.html#sklearn.metrics.multilabel_confusion_matrix)

在计算分类多标记混淆矩阵C时 ，类的TN计数i是Ci,0,0，FN是Ci,1,0，TP性是Ci,1,1，FP是Ci,0,1

- 别名：multilabel_confusion_matrix

- 输入

  - **predictionCol**：预测类目，默认值"prediction"

    数据类型：

    ​	multiclass：Double

    ​	multilabel：Array[Double]或Vector

  - **labelCol**：真实类目，默认值"label"，数据类型同predictionCol

  - **weightCol**：可选，样本权重，数据类型Double

  

#### HammingLoss

计算两组样本之间的 average Hamming loss（平均汉明损失）。详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.hamming_loss.html#sklearn.metrics.hamming_loss)

- 别名：hamming_loss，hamming-loss，hammingloss
  
- 输入
  
- **predictionCol**：预测类目，默认值"prediction"
  
  数据类型：
  
  ​	binary/multiclass：Double
  
  ​	multilabel：Array[Double]或Vector
  
- **labelCol**：真实类目，默认值"label"，数据类型同predictionCol
  
- **weightCol**：可选，样本权重，数据类型Double
  
    

#### HingeLoss

Hinge loss被用于最大间隔分类器上：比如SVM。详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.hinge_loss.html#sklearn.metrics.hinge_loss)

- 别名：hinge_loss，hinge-loss，hingeloss
  
- 输入
  
- **rawPredictionCol**：预测正样本概率或概率分布，数据类型Double，Array[Double]或Vector
  
- **labelCol**：真实类目，默认值"label"，数据类型Double
  
- **weightCol**：可选，样本权重，数据类型Double
  
    

#### LogLoss

计算交叉熵损失，`-log P(yt|yp) = -(yt log(yp) + (1 - yt) log(1 - yp))`。详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.log_loss.html#sklearn.metrics.log_loss)

- 别名：log_loss，log-loss，logloss
  
- 输入
  
  - **rawPredictionCol**：预测类目，默认值"rawPredictionCol"，数据类型Double，Array[Double]或Vector
  - **labelCol**：真实类目，默认值"label"，数据类型Double
- **weightCol**：可选，样本权重，数据类型Double
  
- 参数

  - **normalize**：默认值true，返回归一化值；若为false，返回损失和

  - **epsilon**：可选，p=0或p=1时，损失函数未定义，p被裁剪为`max(eps, min(1 - eps, p))`

    

#### ZeroOneLoss

计算零一损失 sum（和）或average（平均值），默认计算均值。详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.zero_one_loss.html#sklearn.metrics.zero_one_loss)

- 别名：zero_one_loss，zero-one-loss，zerooneloss
  
- 输入
  
- **predictionCol**：预测类目，默认值"prediction"
  
  数据类型：
  
  ​	binary/multiclass：Double
  
  ​	multilabel：Array[Double]或Vector
  
- **labelCol**：真实类目，默认值"label"，数据类型同predictionCol
  
- **weightCol**：样本权重，默认值"weight"，数据类型Double
  
- 参数

  - **normalize**：默认值true，返回归一化值；若为false，返回误分类样本个数

    

#### BrierScoreLoss

计算实际结果与可能结果的预测概率之间均方差的得分。详细参数说明查看[Sklearn文档](https://scikit-learn.org/stable/modules/generated/sklearn.metrics.brier_score_loss.html#sklearn.metrics.brier_score_loss) 

实际结果必须为1或0（真或假），而实际结果的预测概率可以是0到1之间的值。

Brier 分数损失也在0到1之间，分数越低（均方差越小），预测越准确。

- 别名：brier_score_loss，brier-score-loss，brierscoreloss
- 输入
  - **rawPredictionCol**：预测正样本概率或概率分布，数据类型Double，Array[Double]或Vector
  - **labelCol**：真实类目，默认值"label"，数据类型同predictionCol
  - **weightCol**：样本权重，默认值"weight"，数据类型Double



### 回归评估指标

#### MaxError

- 别名：max_error

- 输入

  - **predictionCol**：预测值，默认值"prediction"，数据类型Double

  - **labelCol**：真实值，默认值"label"，数据类型Double

    

#### MeanAbsoluteError

- 别名：mae，mean_absolute_error

- 输入

  - **predictionCol**：预测值，默认值"prediction"，数据类型Double

  - **labelCol**：真实值，默认值"label"，数据类型Double

    

#### MeanSquaredError

- 别名：mse，mean_squared_error"
- 输入
  - **predictionCol**：预测值，默认值"prediction"，数据类型Double
  - **labelCol**：真实值，默认值"label"，数据类型Double



#### R2Score

- 别名：r2_score，r2
- 输入
  - **predictionCol**：预测值，默认值"prediction"，数据类型Double
  - **labelCol**：真实值，默认值"label"，数据类型Double



#### RootMeanSquaredError

- 别名：root_mean_squared_error，rmse
- 输入
  - **predictionCol**：预测值，默认值"prediction"，数据类型Double
  - **labelCol**：真实值，默认值"label"，数据类型Double



### 排序评估指标

#### AveragePrecisionScore

- 别名：average_precision，AP
- 输入
  - **predictionCol**：预测值，默认值"prediction"，数据类型Array[Double]
  - **labelCol**：真实值，默认值"label"，数据类型Array[Double]
- 参数
  - **k**：可选，如果未指定k，返回MAP值；若指定k，返回top K的精度



#### NdcgScore

- 别名：average_precision，AP

- 输入

  - **predictionCol**：预测值，默认值"prediction"，数据类型Array[Double]
  - **labelCol**：真实值，默认值"label"，数据类型Array[Double]

- 参数

  - **k**：默认值10

    

## 分类模型

### <span id="fm">FMWithSGDClassifier</span>

FM算法的分布式实现，基于随机梯度下降法，2分类

- 输入/输出

  - **featuresCol**: 特征向量，数据类型Vector
  - **labelCol**: 样本类目，取值1.0/0.0，数据类型Double
  - **rawProbabilityCol**：概率分布，数据类型Vector
  - **probabilityCol**：预测类目概率，类型Double
  - **predictionCol**：预测类目，类型Double

- 参数

  - **maxIter**: 最大迭代次数，默认100

  - **stepSize**: 学习率，默认1.0

  - **fitIntercept**: 是否有intercept项，默认true

  - **fitLinear**：是否有1-way项，默认true

  - **vectorSize**：向量维度，默认8

  - **interceptReg**：intercept正则化系数，默认0

  - **oneWayReg**：1-way正则化系数，默认0.001

  - **twoWayReg**：2-way正则化系数，默认0.0001

  - **initStd**: 向量初始化标准差，默认0.01

    

### FFMClassifier

FFM算法的分布式实现，2分类

- 输入/输出

  - **featuresCol**: 特征向量，数据类型String，数据格式fieldID:featureID:value
  - **labelCol**: 样本类目，取值1.0/0.0，数据类型Double
  - **rawProbabilityCol**：概率分布，数据类型Vector
  - **probabilityCol**：预测类目概率，类型Double
  - **predictionCol**：预测类目，类型Double

- 参数

  - **maxIter**: 最大迭代次数，默认100

  - **stepSize**: 学习率，默认0.1

  - **fitIntercept**: 是否有intercept项，默认true

  - **fitLinear**：是否有1-way项，默认true

  - **solver**：优化器，adagrad/sgd，默认adagrad

  - **numField**：field数量，默认max(field_id)

  - **numFeature**：feature数量，默认max(feature_id)

  - **vectorSize**：向量维度，默认8

  - **twoWayReg**：2-way正则化系数，默认0.0002

  - **standardization**：是否对特征归一化，默认true

  - **random**：数据是否shuffle，默认false

    

## 回归模型

### FMWithSGDRegression

FM算法的分布式实现，基于随机梯度下降法，回归

- 输入/输出

  - **featuresCol**: 特征向量，数据类型Vector
  - **labelCol**: 拟合值，数据类型Double
  - **predictionCol**：预测类目，类型Double

- 参数同[FMWithSGDClassifier](#fm)

  

## 图模型

### RandomWalk

[Node2vec]([chrome-extension://cdonnmffkdaoajfknoeeecmchibpmkmg/assets/pdf/web/viewer.html?file=https%3A%2F%2Fcs.stanford.edu%2F~jure%2Fpubs%2Fnode2vec-kdd16.pdf](chrome-extension://cdonnmffkdaoajfknoeeecmchibpmkmg/assets/pdf/web/viewer.html?file=https%3A%2F%2Fcs.stanford.edu%2F~jure%2Fpubs%2Fnode2vec-kdd16.pdf))随机游走，基于[开源项目](https://github.com/aditya-grover/node2vec)封装

- 输入/输出

  - **srcCol**：始节点，默认值src，数据类型String
  - **dstCol**：终节点，默认值dst，数据类型String
  - **weightCol**：边权重，默认值weight，权重默认值1，数据类型String/Double
  - **outputCol**：游走路径，默认值path，数据类型Array[String]

- 参数

  - **walkLength**：游走的序列长度，默认80

  - **numWalks**：游走迭代次数，默认10
  - **p**：return parameter，默认1.0
  - **q**：in-out parameter，默认1.0
  - **degree**：节点的最大度，默认30
  - **directed**：是否是有向图，默认false
  - **numPartition**：分区数，默认200
  - **indexed**：节点是否已经是索引，若是则直接转为Long，否则构建索引，默认false
  - **mergeEdge**：随机游走前是否对相同的边合并权重



## ThirdParty

EasyML依赖了常用机器学习开源项目，这些算子的API请参考官方文档

### Spark ML

- 版本：2.3.2
- [官方文档](https://spark.apache.org/docs/2.3.2/api/scala/index.html#org.apache.spark.package)



### Xgboost

-  版本：0.82
- [官方文档](https://xgboost.readthedocs.io/en/release_0.82/parameter.html)



### LightGBM

- 版本：2.2.350

-  [官方文档](https://lightgbm.readthedocs.io/en/latest/Parameters.html)



### BigDL

- 版本：0.10.0
- [官方文档](https://bigdl-project.github.io/0.10.0/index.html)

