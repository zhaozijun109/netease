# 创作者贡献度计算 — 开发归档

> 开发周期：2026-03-24 ~ 2026-03-27  
> 项目路径：`notebooks/alg/creator_contribution_degree/`

---

## 零、上线情况

| 项目 | 详情                                                                                                                                                                                                                                                                |
|------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| **平台** | 猛犸数据平台 (EasyData)                                                                                                                                                                                                                                                 |
| **任务名** | 高留存内容池                                                                                                                                                                                                                                                            |
| **上线日期** | 2026-03-27                                                                                                                                                                                                                                                        |
| **任务链接** | [高留存内容池](https://easydata-gy.netease.com/easydev/#/activity/develop?projectName=f1b73c5447414cc2b4c0573d1b80aa05$$dev&projectAliasName=高留存内容池&flowName=高留存内容池&flowAliasName=高留存内容池&mode=dev&isProject=false&env=eyJwcm9kdWN0IjoicmVjIiwiY2x1c3RlcmlkIjoiaHo4In0=) |
| **输出表** | `rec.rec_boost_article_contribution_score`（文章级，按 dt 分区）<br>`rec.rec_boost_creator_contribution_score`（创作者级，按 dt 分区）                                                                                                                                               |

---

## 一、背景

在内容社区 Lofter 中，需要量化每个创作者对社区的"贡献度"，以此设计流量扶持方案——高贡献度创作者获得更多流量扶持，最终目标是提升创作者发文粘性。

核心命题：**创作者的内容是否真的因果性地帮助了用户留存？** 不能简单用阅读量或互动量衡量，因为热门创作者的读者本来就是高活跃用户（混淆偏差）。需要用因果推断方法分离出内容本身对留存的净贡献。

## 二、目标

1. 构建因果推断模型，估计每篇文章对用户留存的**因果效应**（而非相关性）
2. 输出**文章级贡献度**和**创作者级贡献度**，含排名和等级
3. 支持亿级数据量处理
4. 核心互动用户的权重更高（评论/收藏/点赞活跃的用户对社区更重要）

## 三、最终方案概述

采用 **Double Machine Learning (DML)** 因果推断框架：

```
用户-文章宽表(~1亿行)
    ↓ PySpark 特征工程
用户-日聚合表(~252w行)
    ↓ CausalForestDML (EconML + LightGBM)
用户级 CATE (条件平均因果效应)
    ↓ 按 engagement_score 分配回文章
文章贡献度 → 创作者贡献度
    ↓ 写入 Hive 分区表
rec.article_contribution_score / rec.creator_contribution_score
```

提供两种可运行方案：
- **方案 A**：全 PySpark Pipeline（pandas_udf 预测），适合生产调度
- **方案 B**：PySpark 导出 + 本地 Python 建模，适合快速验证（已完整跑通）

## 四、实现过程中的关键点

### 4.1 需求迭代：从 4 张表到 1 张宽表

初始设计基于 4 张独立的源表（user_retention_label、user_article_interaction、article_features、user_profile_daily），编写了完整的 JOIN 预处理代码。后来数据侧已提前做好宽表，输入简化为 1 张用户×文章粒度的宽表（~1亿行），字段名也全部变化。

**影响**：Step 1 数据预处理从"4表 JOIN"简化为"特征工程 + 聚合"；字段映射全部重写。

### 4.2 DML 模型的关键调参

#### 问题 1：LGBMClassifier 报错
```
AttributeError: Cannot use a classifier as a first stage model when the target is continuous!
```
**根因**：EconML 的 CausalForestDML 对 `model_y` 调用 `.predict()` 而非 `.predict_proba()`，当 Y 为 float 类型时（即使值只有 0.0/1.0），Classifier 报错。  
**修复**：binary outcome 也统一用 `LGBMRegressor`，输出连续概率值。

#### 问题 2：CATE 零异质性（所有用户效应完全相同）

首次训练结果：
```
ATE = 1.1e-05, CATE std = 8.5e-20（≈0）
p10 = p50 = p90 = 1.1e-05
```
Causal Forest 退化成了常数。

**根因 + 修复（3 个改动）**：

| 问题 | 旧值 | 新值 | 原因 |
|------|------|------|------|
| Treatment 无变换 | 原始 T (范围 [0, 100+]) | `log(1+T)` | 极端值压低单位效应至 1e-5，log 后量级合理 |
| `min_samples_leaf` 太大 | 100 | 20 | 250w 样本中叶子至少 100 个，树无法分裂 |
| `min_impurity_decrease` 太大 | 0.001 | 1e-8 | 效应量级本身 1e-5，0.001 阈值挡住所有分裂 |

修复后结果：
```
retained_1d: ATE=0.064, CATE std=0.049, p10=0.008, p90=0.135 (16.8x)
retained_3d: ATE=0.044, CATE std=0.032, p10=0.008, p90=0.086 (11.4x)
```

### 4.3 duration 归一化设计

阅读时长范围 5s~10min，需映射到 [0, 5]（与 is_follow=5 权重对齐）。

初始方案用线性归一化 `clip(0,300)/60`，5s 只得 0.08 分，低时长区分度极差。

最终采用 **对数变换**：`5 × log(clip(duration, 5, 600) / 5) / log(120)`
- 体现边际递减（5s→30s 价值提升大，5min→10min 提升小）
- 低时长区分度高（5s→0, 15s→1.15, 30s→1.87）
- 满分 5 分对应 10 分钟

### 4.4 用户权重体系

基于宽表可用特征，按核心互动度分 4 级：
- 核心互动(评论 P75+)：2.5×
- 活跃互动(点赞/收藏 P75+)：2.0×
- 高活跃(30天活跃≥20天)：1.5×
- 普通：1.0×

取最大值，用于聚合阶段加权。

### 4.5 模型诊断体系

建立了自动化的诊断框架，训练完成后自动输出：
- ATE 方向和置信区间检查
- CATE mean vs ATE 一致性检查
- 异质性评估（CV 值）
- 效应符号分布（p10 是否为负）
- 效应范围（p90/p10 比值）

每项指标带 ✅🟡🔴 标记，无需人工判断。

## 五、达成情况

### 5.1 模型效果

| 指标 | retained_1d | retained_3d |
|------|-------------|-------------|
| ATE | 0.064 (6.4%) | 0.044 (4.4%) |
| CATE CV | 0.76 | 0.73 |
| p90/p10 | 16.8x | 11.4x |
| CATE 全正 | ✅ | ✅ |
| 1d > 3d | ✅ 符合衰减预期 | — |

**解读**：用户交互量（log 尺度）每增加 1 单位（约翻 2.7 倍），次日留存概率提升 6.4%，3 日留存提升 4.4%。高价值用户的边际效应是低价值用户的 11~17 倍。

### 5.2 产出物

| 文件 | 说明 | 状态 |
|------|------|------|
| `proposal.md` | 需求文档 | ✅ |
| `technical_design.md` | 技术方案（含 DML 原理详解） | ✅ |
| `plan_a_spark_pipeline.py` | 方案 A：全 PySpark Pipeline | ✅ 代码完成 |
| `plan_b_step1_export.py` | 方案 B Step1：PySpark 特征工程 + 导出 | ✅ 已跑通 |
| `plan_b_step2_local_dml.py` | 方案 B Step2：本地 DML 训练 + 预测 | ✅ 已跑通 |
| `plan_b_step3_aggregate.py` | 方案 B Step3：PySpark 贡献度聚合 + Hive 输出 | ✅ 代码完成 |
| `requirements.txt` | Python 依赖 | ✅ |
| `dml_models/dml_models.pkl` | 训练好的 DML 模型 | ✅ 已保存 |
| `contribution_export/` | 中间数据（user_day_agg, user_article_detail, cate） | ✅ 已导出 |

### 5.3 输出表

- `rec.article_contribution_score`（按 dt 分区）：文章级贡献度、排名、百分位
- `rec.creator_contribution_score`（按 dt 分区）：创作者级贡献度、排名、百分位、S/A/B/C/D 等级

## 六、后续优化点

### 6.1 模型层面

| 优化点 | 说明 | 优先级 |
|--------|------|--------|
| **ATE 置信区间** | 当前 CI 未计算成功（可能是数据量或 EconML 版本问题），需排查 `ate_inference` | 高 |
| **ATE 偏大问题** | retained_1d 的 ATE=0.064 触发了"可能存在残余混淆"的警告，可尝试增加 Confounders（如文章类型、标签等） | 高 |
| **多留存指标融合** | 当前只用了 retained_1d 和 retained_3d，retain_active_days_7d 被注释掉了，可重新加入 | 中 |
| **Treatment 定义** | 当前用 log(1+总交互得分)，可尝试其他定义如阅读文章数、总停留时长 | 中 |
| **Nuisance model 调优** | 一阶段 LightGBM 的拟合质量直接影响 CATE 估计，可做 CV 评估 R² | 中 |
| **LinearDML 对比** | CausalForestDML 之外，用 LinearDML 做基线对比，检查 ATE 一致性 | 低 |

### 6.2 特征层面

| 优化点 | 说明 | 优先级 |
|--------|------|--------|
| **文章特征未使用** | 宽表中的 tags、article_type、words_count、photo_num 等未进入模型，加入后可能改善混淆去除效果 | 高 |
| **用户新老标记** | 原始需求提到新用户/回流用户权重更高，但宽表中没有此字段，可后续补充 | 中 |
| **时间特征** | 可加入星期几、是否节假日等特征，捕获时间维度的混淆 | 低 |

### 6.3 工程层面

| 优化点 | 说明 | 优先级 |
|--------|------|--------|
| **方案 A 实测** | 方案 A（pandas_udf 全量预测）已编码但未实际运行验证 | 高 |
| **日常调度** | 包装成可定时调度的 Pipeline（如 Airflow DAG） | 中 |
| **增量更新** | 当前是全量重算，可改为增量（每天只算新增数据的贡献度） | 中 |
| **模型版本管理** | dml_models.pkl 只保存了最新一个，可增加版本化管理 | 低 |

### 6.4 业务层面

| 优化点 | 说明 | 优先级 |
|--------|------|--------|
| **流量扶持策略设计** | 基于创作者贡献度等级(S/A/B/C/D)设计具体的流量扶持规则 | 高 |
| **AB 实验验证** | 对高贡献度创作者加大流量扶持后，验证是否提升了社区整体留存 | 高 |
| **创作者反馈闭环** | 将贡献度分数透传给创作者，激励其持续产出高质量内容 | 中 |
| **防刷机制** | 需防止创作者通过水军刷阅读/互动来提升贡献度分数 | 中 |

## 七、技术栈

| 组件 | 版本/说明 |
|------|----------|
| Python | 3.8 (conda env: py38) |
| EconML | ≥ 0.14.0 (CausalForestDML) |
| LightGBM | ≥ 4.0.0 |
| PySpark | ≥ 3.3.0 |
| Pandas | ≥ 1.5.0 |
| NumPy | ≥ 1.23.0 |
| PyArrow | ≥ 10.0.0 |

## 八、踩坑记录

| # | 问题 | 原因 | 解决方案 |
|---|------|------|---------|
| 1 | `Cannot use a classifier as a first stage model when the target is continuous!` | EconML 对 model_y 调 `.predict()` 不调 `.predict_proba()`，float Y + Classifier 冲突 | binary Y 也用 `LGBMRegressor` |
| 2 | CATE std ≈ 0，所有用户效应完全相同 | `min_impurity_decrease=0.001` 远大于效应量级 1e-5，阻止了所有树分裂 | 降低到 `1e-8` + `min_samples_leaf=20` + Treatment 做 `log(1+T)` |
| 3 | `ModuleNotFoundError: No module named '_distutils_hack'` | conda 环境的 setuptools 版本与 .pth 文件不匹配 | `pip install --force-reinstall setuptools` |
| 4 | Parquet 输出碎片文件过多 | Spark 默认按分区数输出文件 | `coalesce(1)` 合并（注意大数据量 OOM 风险） |

## 九、关键决策记录

| 决策点 | 选项 | 最终选择 | 理由 |
|-------|------|---------|------|
| 因果推断方法 | DML / Uplift Model / DML+Shapley | **DML (CausalForestDML)** | 高维适配、异质性估计、EconML 成熟实现 |
| 处理框架 | 全 PySpark / PySpark+本地Python / 全 Python | **两种方案都做** | A 适合生产，B 适合验证 |
| 留存目标 | 1d / 3d / 7d活跃天数 / 多指标 | **1d + 3d** | 信号直接，样本充足；7d 后续可加 |
| Treatment 变换 | 原始值 / log / 分桶 | **log(1+T)** | 压缩极端值，效应量级合理 |
| duration 归一化 | 线性 / 对数 | **对数** | 边际递减特性，低时长区分度好 |
| Nuisance model | LGBMClassifier / LGBMRegressor | **全用 Regressor** | EconML 兼容性要求 |
