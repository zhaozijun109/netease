# 创作者贡献度计算 — 技术方案

## 一、总体架构

### 1.1 输入

一张已有宽表 `rec.tmp_0324_creator_contribution_degree_input_wide_table`，粒度为**用户 × 文章**，每行代表一条用户阅读文章的交互记录，约 **1 亿行**。包含：
- 用户-文章交互特征（duration, like, collect, comment, share, is_follow）
- 文章特征（blog_id, tags, article_type, hot, ...）
- 用户特征（active_days_*d, send_hot_30d, send_comment_cnt_30d, send_collect_cnt_30d）
- 留存标签（retain_1d, retain_3d, retain_active_days_7d）

### 1.2 输出

- **文章贡献度表**: 每篇文章 (postid) 的留存因果贡献分
- **创作者贡献度表**: 每个创作者 (blog_id) 的综合贡献度及排名

### 1.3 Pipeline 流程

提供两种可运行方案：

```
方案 A: 全 PySpark（适合生产调度）
┌─────────────────────────────────────────────────────────┐
│ Step 1: 特征工程 (PySpark)                               │
│   宽表 → 计算 engagement_score、用户权重                  │
│        → 聚合到 用户-日 粒度 (DML训练用)                  │
│                                                          │
│ Step 2: DML 训练 (PySpark 抽样 → 本地 Python)             │
│   用户-日 聚合表抽样 ~500w → 训练 CausalForestDML          │
│                                                          │
│ Step 3: CATE 预测 + 贡献度聚合 (PySpark + Spark UDF)     │
│   全量用户-日数据 → UDF 调用模型预测 CATE                  │
│   → 按 engagement_score 占比分配到文章                     │
│   → 聚合到文章/创作者                                     │
└─────────────────────────────────────────────────────────┘

方案 B: PySpark 抽样 + 本地 Python 建模（适合快速验证）
┌─────────────────────────────────────────────────────────┐
│ Step 1: PySpark 特征工程 + 导出                           │
│   宽表 → 特征工程 → 全量用户-日聚合表导出为本地 Parquet    │
│                                                          │
│ Step 2: 本地 Python DML 建模 + 全量预测                   │
│   抽样训练 → 分批预测全量数据 → 输出 CATE                  │
│                                                          │
│ Step 3: PySpark 贡献度聚合                                │
│   读回 CATE 结果 → 分配到文章 → 聚合创作者                 │
└─────────────────────────────────────────────────────────┘
```

## 二、核心方法: Double Machine Learning (DML)

### 2.1 为什么选 DML?
- **因果性**: 相比纯相关性模型，DML 通过"双残差"去除混淆偏差，估计的是文章交互对留存的**因果效应**
- **高维适配**: 可使用 LightGBM 作为 nuisance model，天然支持高维特征
- **异质性**: 通过 CATE (Conditional Average Treatment Effect) 可得到每个用户-文章交互的个体化因果效应
- **可扩展**: EconML 库提供成熟实现，支持分批训练

| 要求 | DML 如何满足 |
|------|-------------|
| **因果性** | 双残差去混淆，不是拟合相关性 |
| **高维特征** | 一阶段用 LightGBM，天然处理高维 |
| **异质效应** | Causal Forest 输出每个用户的个性化 CATE |
| **千万数据** | 一阶段 LightGBM 高效；抽样训练 + 分批预测 |
| **可解释** | ATE 有置信区间，feature_importance 可解释 |
| **灵活性** | 多目标(1d/7d/活跃天数)分别训练，独立融合 |

> **一句话总结**: DML 让我们能用任意强大的 ML 模型（LightGBM）处理混淆变量，同时保持因果估计的统计有效性（√n-一致性和渐近正态性）。这是传统因果推断方法（如倾向得分匹配）做不到的。

### 2.2 要解决的核心问题

我们要回答：**用户多看一篇文章（多一单位交互），对 TA 明天是否回来（留存）的因果效应有多大？**

注意是**因果效应**，不是相关性。直接拿"交互量"和"留存"做回归会有严重偏差——因为**活跃用户本来就既看得多、又容易留存**，二者都是用户本身特质（混淆变量）驱动的：

```
        混淆变量 X（用户活跃度、注册天数...）
           /              \
          ↓                ↓
    T（交互得分）  →??→  Y（次日留存）
```

DML 的目标就是**去掉 X 的干扰，精准估计 T→Y 的因果箭头**。

### 2.3 DML 核心原理：双残差（Double Residualization）

#### Stage 1 — "把混淆因素的影响先剥干净"

假设我们有：
- Y：次日是否留存
- T：用户当天总交互得分
- X：用户画像 + 内容特征（混淆变量）

DML 先用两个独立的 ML 模型（这里用 LightGBM）分别做预测：

| 模型 | 任务 | 直觉含义 |
|------|------|---------|
| ĝ(X) = E[Y \| X] | 仅用用户特征预测留存 | "这个用户本来就有多大概率留存" |
| m̂(X) = E[T \| X] | 仅用用户特征预测交互 | "这个用户本来就会看多少文章" |

然后取**残差**：

```
Ỹ = Y - ĝ(X)    （留存中"超出预期"的部分）
T̃ = T - m̂(X)    （交互中"超出预期"的部分）
```

**残差的含义举例**：
- Ỹ：用户 A 本来 70% 概率留存，结果真的留了 → Ỹ = 1 - 0.7 = 0.3（留存比预期好了一点）
- T̃：用户 A 通常交互 5 分，今天交互了 8 分 → T̃ = 8 - 5 = 3（看得比平时多了）

这两个残差已经**去掉了用户本身特质的影响**，是"纯粹的意外波动"。

#### Stage 2 — "用残差估计因果效应"

现在问题变成：**交互"意外地多"了一些，留存是否也"意外地好"了一些？**

如果是**常数效应**（所有用户效应相同）：
```
θ = E[Ỹ · T̃] / E[T̃²]
```
这就是经典的 IV/控制函数估计量，一步搞定。

但我们需要**异质因果效应 CATE**——不同用户的效应不同（新用户 vs 老用户），所以在 Stage 2 用 **Causal Forest** 来拟合 θ(X)：
```
θ(X_i) = CausalForest(Ỹ_i, T̃_i, X_i)
```

#### 为什么要"Double"？——正则化偏差消除

这是 DML 最精妙的地方。只做一次残差化（比如只对 Y 去混淆）会导致**正则化偏差**：ML 模型的 ĝ(X) 不可能完美，残差里会残留 X 的信息，这些信息通过 T 又会污染因果估计。

DML 同时对 Y 和 T 做残差化，二者的偏差**在乘积中相消**（数学上是二阶小量）：
```
Bias ∝ (g - ĝ) × (m - m̂) ≈ 0
       ─────────   ─────────
       Y模型误差   T模型误差
```
这就是 **Neyman 正交性（Neyman Orthogonality）**，也是名字中"Double"的来源。

### 2.4 Cross-Fitting——避免过拟合

DML 还有一个关键操作：**样本分割（Cross-Fitting）**。

如果用**全部数据**训练 ĝ 和 m̂，再在**同一批数据**上算残差，ML 模型的过拟合会让残差偏小，导致估计偏差。

Cross-Fitting（代码中 `cv=3`）的做法：
```
数据分3折: [Fold1] [Fold2] [Fold3]

对 Fold1 的残差:
  用 Fold2+Fold3 训练 ĝ, m̂ → 在 Fold1 上预测 → 算 Fold1 的残差

对 Fold2 的残差:
  用 Fold1+Fold3 训练 ĝ, m̂ → 在 Fold2 上预测 → 算 Fold2 的残差

对 Fold3 的残差:
  用 Fold1+Fold2 训练 ĝ, m̂ → 在 Fold3 上预测 → 算 Fold3 的残差
```
每个样本的残差都是**用没见过该样本的模型**算出来的，彻底避免过拟合。

### 2.5 完整数据流示例

用一个具体用户举例走通全流程：

```
用户张三, 2026-03-20, 在宽表中有5条记录(看了5篇文章):
  用户特征 X: 前7天活跃5天, 前30天点赞80次, 前30天评论20次...
  文章交互:
    文章P1: duration=120s, 点赞1次, 评论1次 → engagement_score=7.0
    文章P2: duration=60s,  点赞1次           → engagement_score=3.0
    文章P3: duration=30s,  无互动             → engagement_score=0.5
    文章P4: duration=90s,  收藏1次           → engagement_score=3.5
    文章P5: duration=20s,  无互动             → engagement_score=0.33
  T(当天总交互得分) = 7.0+3.0+0.5+3.5+0.33 = 14.33
  Y: 次日确实回来了 (retain_1d = 1)

Step 1 — Nuisance Models (LightGBM):
  ĝ(X_张三) = 0.72   ← "像张三这种活跃用户, 一般72%概率次留"
  m̂(X_张三) = 11.0   ← "像张三这种用户, 一般总交互11分"

  残差:
  Ỹ = 1 - 0.72 = 0.28    ← "留存比预期好了0.28"
  T̃ = 14.33 - 11.0 = 3.33 ← "交互比预期多了3.33分"

Step 2 — Causal Forest:
  CATE(X_张三) = 0.015
  → 含义: 张三每多1分交互, 次日留存概率因果性地提高1.5%

Step 3 — 分配到文章:
  文章P1对张三留存的因果贡献:
    = CATE(张三) × engagement_score(P1)
    = 0.015 × 7.0 = 0.105

  张三是核心互动用户(评论多), user_weight = 2.5:
    文章P1的加权贡献 = 0.105 × 2.5 = 0.2625

文章P1的总贡献度 = Σ 所有读者的加权贡献
创作者的贡献度 = Σ 其所有文章的贡献度
```

### 2.6 对应代码示意

```python
from econml.dml import CausalForestDML

dml = CausalForestDML(
    model_y=LGBMClassifier(...),   # ĝ(X): 预测留存
    model_t=LGBMRegressor(...),    # m̂(X): 预测交互
    n_estimators=200,              # Causal Forest 树数量
    cv=3,                          # 3-fold cross-fitting
)

# EconML 的参数约定:
#   Y: outcome (留存)
#   T: treatment (交互得分)
#   X: effect modifiers (影响CATE异质性的特征)
#   W: controls/confounders (纯混淆变量)
dml.fit(Y=Y, T=T, X=effect_modifiers, W=confounders)

# 预测每个用户的 CATE
cate = dml.effect(X=effect_modifiers)  # shape: (n_users, 1)

# ATE (平均因果效应) + 置信区间
ate_inf = dml.ate_inference(X=effect_modifiers)
# → ATE = 0.012, 95% CI = [0.009, 0.015]
# 含义: 平均来看, 每多1分交互, 留存概率提高1.2%
```

### 2.7 模型变量定义（基于新宽表）

#### Treatment (处理变量 T)
每个用户当天的**总交互得分**（连续变量），需从用户-文章粒度聚合到用户-日粒度:
```
T = Σ engagement_score(user, article)
```
其中单篇文章交互得分:
```
engagement_score = duration_norm + 2×like_recommend_pv + 3×comment_pv
                   + 2×share_pv + 2×collect_pv + 5×is_follow
```
duration 归一化采用 **对数变换**，映射到 [0, 5]:
```
duration_norm = 5 × log(clip(duration, 5, 600) / 5) / log(120)
```
用 log 变换是因为阅读时长的边际效应递减（从5s→30s的提升远大于5min→10min），对照表:

| 时长 | duration_norm |
|------|--------------|
| 5s   | 0.00         |
| 15s  | 1.15         |
| 30s  | 1.87         |
| 60s  | 2.57         |
| 120s | 3.27         |
| 300s | 4.18         |
| 600s | 5.00         |

#### Outcome (结果变量 Y)
多个留存指标（同一用户同一天取值相同）:
- `retain_1d`: 次日留存 (0/1)
- `retain_3d`: 3日留存 (0/1)
- `retain_active_days_7d`: 后7天活跃天数

#### Confounders / Controls (混淆变量 W)
用户侧（从宽表去重取出）:
- active_days_1d, active_days_3d, active_days_7d, active_days_15d, active_days_30d
- send_hot_30d, send_comment_cnt_30d, send_collect_cnt_30d

#### Effect Modifiers (效应修饰变量 X)
影响 CATE 异质性的用户特征子集:
- active_days_7d, active_days_30d
- send_hot_30d, send_comment_cnt_30d, send_collect_cnt_30d

以及内容侧聚合特征:
- 当天阅读文章数, 阅读不同创作者数, 平均文章热度

## 三、用户权重设计

基于宽表中可用的用户特征:

| 用户类型 | 条件 | 权重 | 理由 |
|---------|------|------|------|
| 核心互动用户 | send_comment_cnt_30d > P75 | 2.5 | 爱评论的用户对社区氛围贡献大 |
| 活跃互动用户 | send_hot_30d > P75 或 send_collect_cnt_30d > P75 | 2.0 | 点赞/收藏活跃 |
| 高活跃用户 | active_days_30d >= 20 | 1.5 | 稳定活跃的社区基石 |
| 普通用户 | 其他 | 1.0 | 基准 |

权重取最大值，用于贡献度聚合阶段加权求和。

## 四、贡献度计算

### 4.1 文章级贡献度
对文章 a 的贡献度:
```
article_contribution(a) = Σ_{u ∈ readers(a)} CATE(u) × engagement_score(u, a) × user_weight(u)
```
其中 `CATE(u)` 是用户 u 每单位交互对留存的因果效应。

### 4.2 创作者级贡献度
对创作者 c:
```
creator_contribution(c) = Σ_{a ∈ articles(c)} article_contribution(a)
```

### 4.3 多留存指标融合
最终贡献度 = 0.4 × contribution_1d + 0.3 × contribution_3d + 0.3 × contribution_active_days_7d (归一化后)

## 五、大数据量处理策略（1亿行）

### 方案 A: 全 PySpark + Spark UDF
1. **PySpark 特征工程**: 在 Spark 中完成 engagement_score 计算、用户-日聚合、用户权重
2. **抽样训练**: 用户-日聚合后约 2000w 行，分层抽样 ~500w 行 → toPandas() → 本地训练 DML
3. **Spark UDF 预测**: 将训练好的模型通过 pandas_udf 广播到 Spark，全量用户-日数据直接在 Spark 中预测 CATE
4. **Spark 聚合**: CATE 按 engagement_score 分配回文章，聚合到文章/创作者

### 方案 B: PySpark 抽样 + 本地 Python
1. **PySpark 特征工程**: 同方案 A
2. **导出本地**: 全量用户-日聚合表导出为本地 Parquet（~2000w行，约2-4GB）
3. **本地 Python 训练 + 全量预测**: 用 EconML 训练，分批预测全量
4. **上传回 Spark**: CATE 结果上传 HDFS，Spark 做最终聚合

## 六、输出表结构

```SQL
-- 文章贡献度表
CREATE TABLE article_contribution_score (
    postid            BIGINT,
    blog_id           STRING,
    contribution_1d   FLOAT,    -- 次日留存贡献度
    contribution_3d   FLOAT,    -- 3日留存贡献度
    contribution_active_days FLOAT, -- 活跃天数贡献度
    contribution_final FLOAT,   -- 综合贡献度
    reader_count      INT,      -- 阅读用户数
    weighted_reader_count FLOAT,-- 加权阅读用户数
    contribution_rank  INT,     -- 贡献度排名
    contribution_percentile FLOAT -- 贡献度百分位
);

-- 创作者贡献度表
CREATE TABLE creator_contribution_score (
    blog_id           STRING,
    article_count     INT,      -- 文章数
    total_reader_count INT,     -- 总阅读用户数
    contribution_1d   FLOAT,
    contribution_3d   FLOAT,
    contribution_active_days FLOAT,
    contribution_final FLOAT,   -- 综合贡献度
    avg_article_contribution FLOAT, -- 篇均贡献度
    contribution_rank  INT,     -- 贡献度排名
    contribution_percentile FLOAT,-- 贡献度百分位
    contribution_level STRING   -- S/A/B/C/D 等级
);
```
