# Sklearn on EasyML

Sklearn是最常用的机器算法库，EasyML支持sklearn训练模型的分布式预测，同时实现目前spark不支持的算子。



## 目标

1. 实现本地python端sklearn训练，spark预测
2. 重写sklearn算子，spark训练&预测



## 已支持算子

1. 若spark原生算子的推断过程与sklearn一致，则只需要继承SklearnReader，实现参数解析和spark算子初始化
2. 若spark原生算子的推断过程与sklearn不一致，则需要重写模型训练和推断过程



### preprocess

| 类名                | spark原生 | 训练 | 预测 |
| ------------------- | --------- | ---- | ---- |
| LabelEncoder        | √         | √    | √    |
| MaxAbsScaler        | √         | √    | √    |
| MinMaxScaler        | √         | √    | √    |
| StandardScaler      | √         | √    | √    |
| Normalizer          | ×         | -    | √    |
| Binarizer           | √         | -    | √    |
| OrdinalEncoder      | ×         | √    | √    |
| OneHotEncoder       | ×         | √    | √    |
| KBinsDiscretizer    | ×         | √    | √    |
| KernelCenterer      | ×         | √    | √    |
| LabelBinarizer      | ×         | √    | √    |
| MultiLabelBinarizer | ×         | √    | √    |
| RobustScaler        | ×         | √    | √    |



### feature_extraction

| 类名              | spark原生 | 训练 | 预测 |
| ----------------- | --------- | ---- | ---- |
| TfidfTransformer  | ×         | √    | √    |
| CountVectorizer   | ×         | √    | √    |
| TfidfVectorizer   | ×         | √    | √    |
| HashingVectorizer | ×         | -    | √    |



### linear_model

| 类名               | spark原生 | 训练 | 预测 |
| ------------------ | --------- | ---- | ---- |
| LogisticRegression | √         | √    | √    |
| LinearSVC          | √         | √    | √    |



### pipeline

| 类名     | spark原生 | 训练 | 预测 |
| -------- | --------- | ---- | ---- |
| Pipeline | √         | -    | √    |



### decomposition

| 类名         | spark原生 | 训练 | 预测 |
| ------------ | --------- | ---- | ---- |
| PCA          | √         | √    | √    |
| TruncatedSVD | ×         | ×    | √    |



### cluster

| 类名            | spark原生 | 训练 | 预测 |
| --------------- | --------- | ---- | ---- |
| KMeans          | √         | √    | √    |
| MiniBatchKMeans | ×         | ×    | √    |
| GaussianMixture | √         | √    | √    |



### naive_bayes

| 类名          | spark原生 | 训练 | 预测 |
| ------------- | --------- | ---- | ---- |
| MultinomialNB | √         | √    | √    |



## 示例

- python

  ```python
  # 构建模型
  text_clf = Pipeline([
    ('tfidf', TfidfVectorizer()),
    ('lr', LogisticRegression()),
  ])
  
  # 训练
  text_clf.fit(train_x, train_y)
  
  # 保存模型
  joblib.dump(text_clf, model_path)
  ```

- spark

  ```scala
  // 读取模型
  val model = SklearnUtils.read(model_path)
  // 预测
  val result = model.transform(df)
  ```


